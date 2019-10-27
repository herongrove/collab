package com.github.danebell.collab

import org.clulab.odin._
import scala.language.implicitConversions

package object mentions {
  type CollabMention = Mention with Dated

  implicit class MentionOps(mention: Mention) {
    def toCollabMention: CollabMention = mention match {
      case m: CollabMention => m

      case m: TextBoundMention =>
        new CollabTextBoundMention(
          m.labels,
          m.tokenInterval,
          m.sentence,
          m.document,
          m.keep,
          m.foundBy
        )

      case m: RelationMention =>
        new CollabRelationMention(
          m.labels,
          convertArguments(m.arguments),
          m.paths,
          m.sentence,
          m.document,
          m.keep,
          m.foundBy
        )

      case m: EventMention =>
        new CollabEventMention(
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

  private def convertArguments(arguments: Map[String, Seq[Mention]]): Map[String, Seq[CollabMention]] =
    arguments.transform {
      case (k, v) => v.map(_.toCollabMention)
    }

}
