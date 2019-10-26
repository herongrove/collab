package com.github.danebell.collab

import org.clulab.processors.Sentence
import org.clulab.processors.clu.SentencePostProcessor

import scala.util.matching.Regex

class CollabPOSPostProcessor extends SentencePostProcessor {

  val LEFT_PARENS: Regex = """^(\-LRB\-)|(\-LSB\-)|(-LCB-)|\(|\[|\{$""".r
  val RIGHT_PARENS: Regex = """^(\-RRB\-)|(\-RSB\-)|(-RCB-)|\)|\]|\}$""".r

  override def process(sentence: Sentence): Unit = {
    val tags = sentence.tags.get

    // change VBN to JJ if in between DT and NN
    for(i <- sentence.indices) {
      if(i > 0 && i < sentence.size - 1 &&
        tags(i) == "VBN" &&
        tags(i - 1).startsWith("DT") &&
        tags(i + 1).startsWith("NN")) {
        tags(i) = "JJ"
      }
    }

    // parens must be tagged -LRB- and -RRB-
    for(i <- sentence.indices) {
      val text = sentence.words(i)
      if(LEFT_PARENS.findFirstMatchIn(text).nonEmpty) {
        tags(i) = "-LRB-"
      } else if(RIGHT_PARENS.findFirstMatchIn(text).nonEmpty) {
        tags(i) = "-RRB-"
      }
    }
  }
}