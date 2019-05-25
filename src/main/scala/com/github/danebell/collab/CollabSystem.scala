package com.github.danebell.collab

import org.clulab.processors.{Document, Processor}
import org.clulab.processors.fastnlp.FastNLPProcessor
import RuleReader._
import com.github.danebell.collab.mentions.CollabMention
import com.github.danebell.collab.utils.DisplayUtils._
import org.clulab.odin.{ExtractorEngine, State}

class CollabSystem(rules: Option[Rules] = None) {


  //val proc: Processor = new FastNLPProcessor() // TODO: Get from configuration file
  val proc: Processor = new CollabProcessor() // TODO: Get from configuration file
  var entityRules: String = if (rules.isEmpty) readResource(RuleReader.entitiesMasterFile) else rules.get.entities
  var eventRules: String = if (rules.isEmpty) readResource(RuleReader.eventsMasterFile) else rules.get.events
  val actions: CollabActions = new CollabActions()

  var entityEngine = ExtractorEngine(entityRules, actions)
  var eventEngine = ExtractorEngine(eventRules, actions)

  def allRules: String =
    Seq(entityRules, eventRules).mkString("\n\n")

  def annotate(text: String, keepText: Boolean = true): Document = {
    val doc = proc.annotate(text, keepText)
    doc
  }

  def extract(text: String): Seq[CollabMention] = {
    val doc = annotate(text)
    extract(doc)
  }

  def extract(doc: Document): Seq[CollabMention] = {
    val entities = entityEngine.extractFrom(doc)
    val events = eventEngine.extractFrom(doc, State(entities))
    events
  }

  def reload(): Unit = {
    this.entityRules = if (rules.isEmpty) readResource(RuleReader.entitiesMasterFile) else rules.get.entities
    this.eventRules = if (rules.isEmpty) readResource(RuleReader.eventsMasterFile) else rules.get.events
    this.entityEngine = ExtractorEngine(this.entityRules, this.actions)
    this.eventEngine = ExtractorEngine(this.eventRules, this.actions)
  }
}