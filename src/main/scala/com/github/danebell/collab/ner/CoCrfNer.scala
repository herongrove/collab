package com.github.danebell.collab.ner

import java.util.Properties

import scala.collection.JavaConverters._
import com.typesafe.scalalogging.LazyLogging
import edu.stanford.nlp.ling.CoreAnnotations.{NormalizedNamedEntityTagAnnotation, SentencesAnnotation, TokensAnnotation}
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import org.clulab.processors.{Document, Processor, Sentence}
import org.clulab.processors.clu.CluProcessor
import org.clulab.processors.clu.tokenizer.{OpenDomainEnglishTokenizer, Tokenizer}
import org.clulab.processors.shallownlp.ShallowNLPProcessor
import org.clulab.processors.shallownlp.ShallowNLPProcessor.cluDocToCoreDoc
import org.clulab.sequences.Tagger

import scala.collection.mutable.ArrayBuffer

/** Draws heavily from org.clulab.processors.shallownlp.ShallowNLPProcessor  */
class CoCrfNer extends Tagger[String] with LazyLogging {
  val internStrings: Boolean = false

  lazy val tokenizer: Tokenizer = new OpenDomainEnglishTokenizer(None)
  lazy val sner: StanfordCoreNLP = mkNer

  def in(s:String): String = {
    if (internStrings) Processor.internString(s)
    else s
  }

  protected def newStanfordCoreNLP(props: Properties, enforceRequirements: Boolean = true): StanfordCoreNLP = {
    // Prevent knownLCWords from changing on us.  To be safe, this is added every time
    // because of potential caching of annotators.  Yes, the 0 must be a string.
    props.put("maxAdditionalKnownLCWords", "0")
    new StanfordCoreNLP(props, enforceRequirements)
  }

  def mkNer: StanfordCoreNLP = {
    val props = new Properties()
    props.put("annotators", "ner")
    newStanfordCoreNLP(props, enforceRequirements = false)
  }

  def basicSanityCheck(sentence: Sentence): Unit = {
    if (sentence.tags.isEmpty)
      throw new RuntimeException("ERROR: you have to run the POS tagger before NER!")
    if (sentence.lemmas.isEmpty)
      throw new RuntimeException("ERROR: you have to run the lemmatizer before NER!")
  }

  def mkDocument(text:String, keepText:Boolean): Document = {
    // create the CLU document
    val doc = CluProcessor.mkDocument(tokenizer, text, keepText = true)

    // now create the CoreNLP document Annotation
    cluDocToCoreDoc(doc, keepText)
  }

  override def find(sentence: Sentence): Array[String] = {
    basicSanityCheck(sentence)

    val doc = new Document(Array(sentence))
    doc.text = Some(sentence.getSentenceText)
    val annotation = ShallowNLPProcessor.docToAnnotation(doc)

    try {
      sner.annotate(annotation)
    } catch {
      case e:Exception =>
        println("Caught NER exception!")
        println("Document:\n" + doc)
        throw e
    }

    // convert CoreNLP Annotations to our data structures
    val sas = annotation.get(classOf[SentencesAnnotation]).asScala
    // there's only one sentence
    val sa = sas.head

    val tb = new ArrayBuffer[String]
    val nb = new ArrayBuffer[String]
    val tas = sa.get(classOf[TokensAnnotation]).asScala
    for (ta <- tas) {
      tb += in(ta.ner())
      val n = ta.get(classOf[NormalizedNamedEntityTagAnnotation])
      //println(s"NORM: $n")
      if (n != null) nb += in(n)
      else nb += in("O")
    }
    tb.toArray
  }

}
