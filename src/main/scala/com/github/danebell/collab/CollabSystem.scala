package com.github.danebell.collab

import org.clulab.processors.{Document, Processor}
import org.clulab.processors.fastnlp.FastNLPProcessor
import RuleReader._
import com.github.danebell.collab.mentions.CollabMention
import com.github.danebell.collab.mentions._
import com.github.danebell.collab.utils.DisplayUtils._
import com.typesafe.scalalogging.LazyLogging
import org.clulab.odin.{ExtractorEngine, State}

class CollabSystem(rules: Option[Rules] = None) extends LazyLogging {

  val proc: Processor = new CollabProcessor()
  var entityRules: String = if (rules.isEmpty) readResource(RuleReader.entitiesMasterFile) else rules.get.entities
  var eventRules: String = if (rules.isEmpty) readResource(RuleReader.eventsMasterFile) else rules.get.events
  val actions: CollabActions = new CollabActions()

  var entityEngine = ExtractorEngine(entityRules, actions)
  var eventEngine = ExtractorEngine(eventRules, actions)

  def allRules: String =
    Seq(entityRules, eventRules).mkString("\n\n")

  def annotate(text: String, keepText: Boolean = true): Document = {
    val doc = proc.mkDocument(text, keepText)
    val relevant = doc
      .sentences
      .filter(_.getSentenceText.contains("consult"))
      .map(_.getSentenceText)
      .mkString(" ")
    val relevantDoc = proc.mkDocument(relevant, keepText)

    if(relevantDoc.sentences.nonEmpty) proc.annotate(relevantDoc)

    relevantDoc
  }

  def extract(text: String): Seq[CollabMention] = {
    val doc = annotate(text)
    extract(doc)
  }

  def extract(doc: Document): Seq[CollabMention] = {
    val entities = try {
      entityEngine.extractFrom(doc)
    } catch { case e: Exception =>
      //e.printStackTrace()
      logger.error(doc.id + " failed because it didn't have entities!")
      Nil
    }
    val events = eventEngine.extractFrom(doc, State(entities))
    val nonOverlapping = MentionFilter.nonIdenticalArgs(events)
    val mostSpecific = MentionFilter.keepMostSpecific(nonOverlapping)

    mostSpecific.map(_.toCollabMention)
  }

  def reload(): Unit = {
    this.entityRules = if (rules.isEmpty) readResource(RuleReader.entitiesMasterFile) else rules.get.entities
    this.eventRules = if (rules.isEmpty) readResource(RuleReader.eventsMasterFile) else rules.get.events
    this.entityEngine = ExtractorEngine(this.entityRules, this.actions)
    this.eventEngine = ExtractorEngine(this.eventRules, this.actions)
  }
}
