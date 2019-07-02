package com.github.danebell.collab

import com.github.danebell.collab.ner.{CoCrfNer, CoHybridNer}
import com.typesafe.config.ConfigFactory
import org.clulab.processors.Document
import org.clulab.processors.clu.CluProcessor
import org.clulab.processors.clu.syntax.EnhancedDependencies
import org.clulab.sequences.Tagger
import org.clulab.struct.GraphMap

class CollabProcessor(
    maxSentenceLength:Int = 80
  ) extends CluProcessor(config = ConfigFactory.load("collabprocessor")) {
  val prefix = "CluProcessor"

  override lazy val ner: Option[Tagger[String]] = {
    getArgString(s"$prefix.ner.type", Some("none")) match {
      case "collab" => Option(new CoHybridNer())
//      case "collab" => Option(new CoCrfNer())
      case "none" => None
      case _ => throw new RuntimeException(s"ERROR: Unknown argument value for $prefix.ner.type!")
    }
  }

  private def basicSanityCheck(doc:Document): Unit = {
    if (doc.sentences == null)
      throw new RuntimeException("ERROR: Document.sentences == null!")
    if (doc.sentences.length != 0 && doc.sentences(0).words == null)
      throw new RuntimeException("ERROR: Sentence.words == null!")
  }

  /** Syntactic parsing; modifies the document in place */
  override def parse(doc:Document) {
    basicSanityCheck(doc)
    if (doc.sentences.head.tags.isEmpty)
      throw new RuntimeException("ERROR: you have to run the POS tagger before parsing!")
    if (doc.sentences.head.lemmas.isEmpty)
      throw new RuntimeException("ERROR: you have to run the lemmatizer before parsing!")

    for {
      sentence <- doc.sentences
      if sentence.words.length <= maxSentenceLength
    } {
      //println(s"PARSING SENTENCE: ${sentence.words.mkString(", ")}")
      //println(sentence.tags.get.mkString(", "))
      //println(sentence.lemmas.get.mkString(", "))
      val dg = depParser.parseSentence(sentence)

      if(useUniversalDependencies) {
        sentence.setDependencies(GraphMap.UNIVERSAL_BASIC, dg)
        sentence.setDependencies(GraphMap.UNIVERSAL_ENHANCED,
          EnhancedDependencies.generateUniversalEnhancedDependencies(sentence, dg))
      } else {
        sentence.setDependencies(GraphMap.STANFORD_BASIC, dg)
        sentence.setDependencies(GraphMap.STANFORD_COLLAPSED,
          EnhancedDependencies.generateStanfordEnhancedDependencies(sentence, dg))
      }
    }
  }

}

