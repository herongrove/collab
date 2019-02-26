package com.github.danebell.collab.ner

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import org.clulab.processors.Sentence
import org.clulab.sequences.{LexiconNER, Tagger}
import org.clulab.utils._

class CoHybridNer(val config: Config = ConfigFactory.load("collabprocessor"))
  extends Tagger[String]
    with Configured
    with LazyLogging {
  val prefix = "CluProcessor"

  override def getConf: Config = config

  lazy val ruleNer: LexiconNER = new CoKbLoader().loadAll()
  lazy val crfNer: CoCrfNer = new CoCrfNer

  override def find(sentence: Sentence): Array[String] = {
    val ruleLabels = ruleNer.find(sentence)
    val crfLabels = crfNer.find(sentence)

    LexiconNER.mergeLabels(ruleLabels, crfLabels)

    ruleLabels
  }
}