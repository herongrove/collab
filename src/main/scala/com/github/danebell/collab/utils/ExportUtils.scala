package com.github.danebell.collab.utils

import com.github.danebell.collab.mentions.{
  CollabMention,
  CollabTextBoundMention,
  CollabRelationMention,
  CollabEventMention
}
import com.github.nscala_time.time.StaticDateTimeFormat
import com.typesafe.scalalogging.LazyLogging

object ExportUtils extends LazyLogging {
  // Format
  // ARG1    EVENT_LABEL   ARG2    DATE    DOC_ID    FOUND_BY    TEXT
  def mentionToTabular(mention: CollabMention, arg1: String = "agent", arg2: String = "theme"): String = mention match {
    // Don't export tbms
    case tbm: CollabTextBoundMention => ""
    case rm: CollabRelationMention => eventToTabular(rm, arg1, arg2)
    case em: CollabEventMention => eventToTabular(em, arg1, arg2)
  }

  def eventToTabular(mention: CollabMention, arg1: String, arg2: String): String = {
    val argument1 = stringifyArg(mention, arg1)
    val lbl = mention.label
    val argument2 = stringifyArg(mention, arg2)
    val date = mention.date.map(d => d.toString(StaticDateTimeFormat.forPattern("yyyy-MM-dd")))
    val docId = mention.document.id.getOrElse("")

    s"$argument1\t$lbl\t$argument2\t$date\t$docId\t${mention.foundBy}\t${mention.text}"
  }

  def stringifyArg(mention: CollabMention, arg: String): String =
    mention.arguments.getOrElse(arg, Nil).map(_.text).headOption.getOrElse("<NULL>")
}