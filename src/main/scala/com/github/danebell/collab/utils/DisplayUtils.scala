package com.github.danebell.collab.utils

import org.clulab.odin._
import org.clulab.processors.{Document, Sentence}

object DisplayUtils {
  protected val nl = "\n"
  protected val tab = "\t"

  def argumentsToString(b: Mention, nl: String, tab: String): String = {
    val sb = new StringBuffer
    b.arguments foreach {
      case (argName, ms) =>
        ms foreach { v =>
          sb.append(s"$tab $argName ${v.labels.mkString("(", ", ", ")")} => ${v.text} $nl")
          if (v.attachments.nonEmpty) sb.append(s"$tab  * Attachments: ${attachmentsString(v.attachments)} $nl")
        }
    }
    sb.toString
  }

  def attachmentsString(mods: Set[Attachment]): String = s"${mods.mkString(", ")}"

  def mentionToDisplayString(mention: Mention): String = {
    val sb = new StringBuffer()
    val boundary = s"$tab ${"-" * 30} $nl"
    sb.append(s"${mention.labels} => ${mention.text} $nl")
    sb.append(boundary)
    sb.append(s"$tab Rule => ${mention.foundBy} $nl")
    val mentionType = mention.getClass.toString.split("""\.""").last
    sb.append(s"$tab Type => $mentionType $nl")
    sb.append(boundary)
    mention match {
      case tb: TextBoundMention =>
        sb.append(s"$tab ${tb.labels.mkString(", ")} => ${tb.text} $nl")
        if (tb.attachments.nonEmpty) sb.append(s"$tab  * Attachments: ${attachmentsString(tb.attachments)} $nl")
      case em: EventMention =>
        sb.append(s"$tab trigger => ${em.trigger.text} $nl")
        if (em.trigger.attachments.nonEmpty) sb.append(s"$tab  * Attachments: ${attachmentsString(em.trigger.attachments)} $nl")
        sb.append(argumentsToString(em, nl, tab) + nl)
        if (em.attachments.nonEmpty) {
          sb.append(s"$tab Event Attachments: ${attachmentsString(em.attachments)} $nl")
        }
      case rel: RelationMention =>
        sb.append(argumentsToString(rel, nl, tab) + nl)
        if (rel.attachments.nonEmpty) {
          sb.append(s"$tab Relation Attachments: ${attachmentsString(rel.attachments)} $nl")
        }
      case _ => ()
    }
    sb.append(s"$boundary $nl")
    sb.toString
  }

  def syntacticDependenciesToString(s:Sentence): String = {
    if (s.dependencies.isDefined) {
      s.dependencies.get.toString
    } else "[Dependencies not defined]"
  }

  def mentionsToDisplayString(
                               mentions: Seq[Mention],
                               doc: Document,
                               printDeps: Boolean = false): String = {

    val sb = new StringBuffer()
    val mentionsBySentence = mentions groupBy (_.sentence) mapValues (_.sortBy(_.start)) withDefaultValue Nil
    for ((s, i) <- doc.sentences.zipWithIndex) {
      sb.append(s"sentence #$i $nl")
      sb.append(s.getSentenceText + nl)
      if (s.lemmas.nonEmpty) sb.append(s.lemmas.get.mkString("", " ", nl))
      if (s.entities.nonEmpty) sb.append(s.entities.get.mkString("", " ", nl))
      sb.append("Tokens: " + (s.words.indices, s.words, s.tags.get).zipped.mkString("", ", ", nl))
      if (printDeps) sb.append(syntacticDependenciesToString(s) + nl)
      sb.append(nl)

      val sortedMentions = mentionsBySentence(i).sortBy(_.label)
      val (events, entities) = sortedMentions.partition(_ matches "Event")
      val (tbs, rels) = entities.partition(_.isInstanceOf[TextBoundMention])
      val sortedEntities = tbs ++ rels.sortBy(_.label)
      sb.append(s"entities: $nl")
      sortedEntities.foreach(e => sb.append(s"${mentionToDisplayString(e)} $nl"))

      sb.append(nl)
      sb.append(s"events: $nl")
      events.foreach(e => sb.append(s"${mentionToDisplayString(e)} $nl"))
      sb.append(s"${"=" * 50} $nl")
    }
    sb.toString
  }

  /* Wrappers for displaying the mention string */
  def displayMentions(mentions: Seq[Mention], doc: Document, printDeps: Boolean = false): Unit = {
    println(mentionsToDisplayString(mentions, doc, printDeps))
  }

}