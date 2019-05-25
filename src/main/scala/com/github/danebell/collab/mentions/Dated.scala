package com.github.danebell.collab.mentions

import com.github.nscala_time.time.Imports._

import org.clulab.odin.Mention

trait Dated {
  this: Mention =>

  var date: Option[DateTime]
}