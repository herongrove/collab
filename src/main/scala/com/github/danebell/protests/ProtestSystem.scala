package com.github.danebell.protests

import org.clulab.processors.{Document, Processor}
import com.github.danebell.protests.RuleReader.Rules
import com.github.danebell.protests.mentions.ProtestMention
import org.clulab.processors.fastnlp.FastNLPProcessor

class ProtestSystem(rules: Option[Rules] = None) {

  val proc: Processor = new FastNLPProcessor() // TODO: Get from configuration file soon

  def annotate(text: String, keepText: Boolean = true): Document = {
    val doc = proc.annotate(text, keepText)
    doc
  }

  def extractFromText(text: String): Seq[ProtestMention] = {
    val doc = annotate(text)
    extractFrom(doc)
  }

  def extractFrom(doc: Document): Seq[ProtestMention] = {
    Nil
  }

  def reload(): Unit = {

  }
}
