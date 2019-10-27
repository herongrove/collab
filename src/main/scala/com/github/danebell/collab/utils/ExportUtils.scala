package com.github.danebell.collab.utils

import com.github.danebell.collab.mentions.{CollabEventMention, CollabMention, CollabRelationMention, CollabTextBoundMention}
import com.github.nscala_time.time.StaticDateTimeFormat
import com.typesafe.scalalogging.LazyLogging
import org.clulab.odin.Mention

object ExportUtils extends LazyLogging {
  // Format
  // EVENT_LABEL    ARG1    ARG1_LABEL   ARG2    ARG1_LABEL    DOC_ID    FOUND_BY    TEXT
  def mentionToTabular(mention: CollabMention, arg1: String = "agent", arg2: String = "theme"): String = mention match {
    // Don't export tbms
    case tbm: CollabTextBoundMention => ""
    case rm: CollabRelationMention => eventToTabular(rm, arg1, arg2)
    case em: CollabEventMention => eventToTabular(em, arg1, arg2)
  }

  def eventToTabular(mention: CollabMention, arg1: String, arg2: String): String = {
    val lbl = mention.label
    val argument1 = stringifyArg(mention, arg1)
    val argument2 = stringifyArg(mention, arg2)
    //val date = mention.date.map(d => d.toString(StaticDateTimeFormat.forPattern("yyyy-MM-dd")))
    val docId = mention.document.id.getOrElse("")

    val context = getContext(mention)

    s"$lbl\t$argument1\t$argument2\t$docId\t${mention.foundBy}\t${mention.text}\t$context"
  }

  def stringifyArg(mention: CollabMention, arg: String): String =
    mention
      .arguments
      .getOrElse(arg, Nil)
      .map{ a => s"${a.text}\t${a.label}" }
      .headOption
      .getOrElse("<NULL>\t<NULL>")

  private def getContext(mention: Mention): String = {
    if (mention.sentence == 0) {
      mention.sentenceObj.getSentenceText
    } else {
      mention.document.sentences(mention.sentence - 1).getSentenceText + " " +
        mention.sentenceObj.getSentenceText
    }
  }
}