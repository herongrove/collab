package com.github.danebell.collab

import com.github.danebell.collab.ner.{CoHybridNer, CoKbLoader}
import com.typesafe.config.ConfigFactory
import org.clulab.processors.clu.CluProcessor
import org.clulab.sequences.Tagger

class CollabProcessor extends CluProcessor(config = ConfigFactory.load("collabprocessor")) {
  val prefix = "CluProcessor"

  override lazy val ner: Option[Tagger[String]] = {
    getArgString(s"$prefix.ner.type", Some("none")) match {
      case "collab" => Option(new CoHybridNer())
      case "none" => None
      case _ => throw new RuntimeException(s"ERROR: Unknown argument value for $prefix.ner.type!")
    }
  }

}