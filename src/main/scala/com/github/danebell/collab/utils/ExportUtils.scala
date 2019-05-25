package com.github.danebell.collab.utils

import com.github.danebell.collab.mentions.{CollabEventMention, CollabMention, CollabRelationMention, CollabTextBoundMention}
import com.github.nscala_time.time.StaticDateTimeFormat
import com.typesafe.scalalogging.LazyLogging

object ExportUtils extends LazyLogging {
  // Format
  // AGENT    EVENT_LABEL   THEME    DATE    DOC_ID    TEXT
  def mentionToTabular(mention: CollabMention): String = mention match {
    // Don't export tbms
    case tbm: CollabTextBoundMention => ""
    case rm: CollabRelationMention => eventToTabular(rm)
    case em: CollabEventMention => eventToTabular(em)
  }

  def eventToTabular(mention: CollabMention): String = {
    val agent = stringifyArg(mention, "agent")
    val lbl = mention.label
    val theme = stringifyArg(mention, "theme")
    val date = mention.date.map(d => d.toString(StaticDateTimeFormat.forPattern("yyyy-MM-dd")))
    val docId = mention.document.id.getOrElse("")

    s"$agent\t$lbl\t$theme\t$date\t$docId\t${mention.text}"
  }

  def stringifyArg(mention: CollabMention, arg: String): String =
    mention.arguments.getOrElse(arg, Nil).map(_.text).headOption.getOrElse("<NULL>")
}