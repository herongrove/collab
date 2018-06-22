package com.github.danebell.protests

import scala.io.Source

/**
  * Utilities to read rule files
  * Derived from org.clulab.reach
  */
object RuleReader {

  case class Rules(entities: String, events: String)

  val resourcesPath = "/com/github/danebell/protests/grammars"
  val entitiesMasterFile = s"$resourcesPath/entities_master.yml"
  val eventsMasterFile = s"$resourcesPath/events_master.yml"

  def readResource(filename: String): String = {
    val source = Source.fromURL(getClass.getResource(filename))
    val data = source.mkString
    source.close()
    data
  }

  def readFile(filename: String): String = {
    val source = Source.fromFile(filename)
    val data = source.mkString
    source.close()
    data
  }

  def mkRules(): Rules  = {
    val entities = readResource(entitiesMasterFile)
    val events = readResource(eventsMasterFile)

    Rules(entities, events)
  }
}
