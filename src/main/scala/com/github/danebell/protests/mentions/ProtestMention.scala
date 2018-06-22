package com.github.danebell.protests.mentions

import org.clulab.odin._
import org.clulab.processors.Document
import org.clulab.struct.Interval

class ProtestTextBoundMention(
  labels: Seq[String],
  tokenInterval: Interval,
  sentence: Int,
  document: Document,
  keep: Boolean,
  foundBy: String
) extends TextBoundMention(labels, tokenInterval, sentence, document, keep, foundBy)

class ProtestRelationMention(
  labels: Seq[String],
  arguments: Map[String, Seq[Mention]],
  paths: Map[String, Map[Mention, SynPath]],
  sentence: Int,
  document: Document,
  keep: Boolean,
  foundBy: String
) extends RelationMention(labels, mkTokenInterval(arguments), arguments, paths, sentence, document, keep, foundBy)

class ProtestEventMention(
  labels: Seq[String],
  trigger: TextBoundMention,
  arguments: Map[String, Seq[Mention]],
  paths: Map[String, Map[Mention, SynPath]],
  sentence: Int,
  document: Document,
  keep: Boolean,
  foundBy: String,
) extends EventMention(labels, mkTokenInterval(trigger, arguments), trigger, arguments, paths, sentence, document, keep, foundBy)
