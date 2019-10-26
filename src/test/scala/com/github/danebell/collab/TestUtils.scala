package com.github.danebell.collab

import com.github.danebell.collab.korrect.KorrectDocuments
import com.github.danebell.collab.mentions.CollabMention

object TestUtils {
  val system = new CollabSystem()

  //val proc = system.proc

  val korrect = KorrectDocuments()

  def getMentions(text: String): Seq[CollabMention] = system.extract(korrect.joinLines(text))
}