package com.github.danebell.collab.mentions

import org.clulab.odin._
import org.clulab.processors.Document
import org.clulab.struct.Interval
import com.github.nscala_time.time.Imports._

class CollabTextBoundMention(
  labels: Seq[String],
  tokenInterval: Interval,
  sentence: Int,
  document: Document,
  keep: Boolean,
  foundBy: String,
  var date: Option[DateTime] = None
) extends TextBoundMention(labels, tokenInterval, sentence, document, keep, foundBy)
  with Dated

class CollabRelationMention(
  labels: Seq[String],
  arguments: Map[String, Seq[Mention]],
  paths: Map[String, Map[Mention, SynPath]],
  sentence: Int,
  document: Document,
  keep: Boolean,
  foundBy: String,
  var date: Option[DateTime] = None
) extends RelationMention(labels, mkTokenInterval(arguments), arguments, paths, sentence, document, keep, foundBy)
  with Dated

class CollabEventMention(
  labels: Seq[String],
  trigger: TextBoundMention,
  arguments: Map[String, Seq[Mention]],
  paths: Map[String, Map[Mention, SynPath]],
  sentence: Int,
  document: Document,
  keep: Boolean,
  foundBy: String,
  var date: Option[DateTime] = None
) extends EventMention(labels, mkTokenInterval(trigger, arguments), trigger, arguments, paths, sentence, document, keep, foundBy)
  with Dated
