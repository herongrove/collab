package com.github.danebell.protests

import org.clulab.odin._

package object mentions {
  type ProtestMention = Mention

  implicit class MentionOps(mention: Mention) {
    def toProtestMention: ProtestMention = mention match {
      case m: ProtestMention => m

      case m: TextBoundMention =>
        new ProtestTextBoundMention(
          m.labels,
          m.tokenInterval,
          m.sentence,
          m.document,
          m.keep,
          m.foundBy
        )

      case m: RelationMention =>
        new ProtestRelationMention(
          m.labels,
          convertArguments(m.arguments),
          m.paths,
          m.sentence,
          m.document,
          m.keep,
          m.foundBy
        )

      case m: EventMention =>
        new ProtestEventMention(
          m.labels,
          m.trigger,
          convertArguments(m.arguments),
          m.paths,
          m.sentence,
          m.document,
          m.keep,
          m.foundBy
        )
    }
  }

  private def convertArguments(arguments: Map[String, Seq[Mention]]): Map[String, Seq[ProtestMention]] =
    arguments.transform {
      case (k, v) => v.map(_.toProtestMention)
    }
}
