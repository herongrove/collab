package com.github.danebell.collab.ner

import ai.lum.common.ConfigUtils._
import com.typesafe.config._
import com.typesafe.scalalogging.LazyLogging
import org.clulab.sequences.{LexiconNER, NoLexicalVariations}
import org.clulab.struct.TrueEntityValidator
import java.io._
import java.util.MissingResourceException
import java.util.zip.{GZIPInputStream, GZIPOutputStream}

//import org.clulab.utils.Files

/**
  * Parent class for classes that load taxonomically defined dictionaries based on tokenized
  * knowledge bases (KBs)
  */
class KbLoader { }

/**
  * [[KbLoader]] for collab.
  */
class CoKbLoader(val config: Config = ConfigFactory.load("reference"))
  extends KbLoader with LazyLogging {

  /** If a serialized NER model already exists, it will be at this location */
  val labNerModel: Option[String] = config.get[String]("collab.ner.model")
  val labKbs: Seq[String] = config[List[String]]("collab.ner.kbs")
  val labOverrides: Option[Seq[String]] =  config.get[List[String]](s"collab.ner.overrides")
  val stopListFile: Option[String] = config.get[String]("collab.ner.stopListFile")

  /** Create a default [[LexiconNER]] from collab config */
  def nerFromKbs(
                  kbs: Seq[String] = labKbs,
                  overrides: Option[Seq[String]] = labOverrides): LexiconNER = {
    LexiconNER(
      kbs,
      overrides,
      new TrueEntityValidator,
      new NoLexicalVariations,
      useLemmasForMatching = false,
      caseInsensitiveMatching = true
    )
  }

  /** The file location of the resources directory */
  val resourceDir: String = {
    // Find the resource path's full file path
    val currentDir = new File(".").getCanonicalPath
    val resourceElements = Seq("reader", "src", "main", "resources")
    (currentDir +: resourceElements).mkString(File.separator)
  }

  /**
    * Serializes a [[LexiconNER]] to a given resource path
    * @param ner The NER to serialize
    * @param modelPath Resource path to export to
    */
  def serializeNer(ner: LexiconNER, modelPath: String): Unit = {
    val modelFile = resourceDir + File.separator + modelPath

    val stream = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(modelFile)))
    stream.writeObject(ner)
    stream.flush()
    stream.close()
  }

  /**
    * Deserializes a [[LexiconNER]] from a given resource path
    * @param modelPath Resource path to import from
    */
  def deserializeNer(modelPath: String): LexiconNER = {
    /*val inDirLoc = resourceDir + File.separator + modelPath

    val stream = if (modelPath.endsWith(".gz")) {
      new ObjectInputStream(new GZIPInputStream(new FileInputStream(inDirLoc)))
    } else {
      new ObjectInputStream(new FileInputStream(inDirLoc))
    }
    val lexiconNer = stream.readObject().asInstanceOf[LexiconNER]
    stream.close()

    val labels = lexiconNer.matchers.map(_._1).sorted
    logger.debug(s"Loaded tries for ${labels.length} labels: ${labels.mkString(", ")}")

    lexiconNer */

    // FIXME: Deserialization of LexiconNER
    logger.warn("Deserialization is not available yet!")
    nerFromKbs()
  }

  /**
    * Returns a [[LexiconNER]], either from a previously computed serialized model, or from the
    * dictionaries themselves, if necessary.
    * @param fromModel True if loading from a serialized model
    * @param serNerLoc The resource path of the serialized model
    */
  def loadAll(
      fromModel: Boolean = false,
      serNerLoc: Option[String] = None): LexiconNER = {
    (fromModel, serNerLoc, labNerModel) match {
      case (false, _, _) => nerFromKbs() // ignore serialized models
      case (true, Some(modelPath), _) => deserializeNer(modelPath) // provide serialized model
      case (true, None, Some(modelPath)) => deserializeNer(modelPath) // default serialized model
      case _ => // no default available
        throw new MissingResourceException(
          "No existing serialized model is available!",
          "LexicalNER",
          "collab.ner.model"
        )
    }
  }
}

object CoKbLoader {
  /**
    * Creates the serialized LexiconNER model from the provided KBs.
    * @param args Any args are ignored. Refer to reference.conf
    */
  def main(args: Array[String]): Unit = {
    val loader = new CoKbLoader()
    // resource path to export the NER model
    val modelPath = loader.config[String]("collab.ner.model")

    // the NER model itself
    val ner = loader.nerFromKbs()

    loader.serializeNer(ner, modelPath)
  }
}
