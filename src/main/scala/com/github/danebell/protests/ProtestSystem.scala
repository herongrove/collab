package com.github.danebell.protests

import com.github.danebell.protests.RuleReader.Rules
import com.github.danebell.protests.mentions.ProtestMention

class ProtestSystem(rules: Option[Rules] = None) {

  def extractFromText(text: String): Seq[ProtestMention] = {
    Nil
  }

  def reload(): Unit = {

  }
}
