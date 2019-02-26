package com.github.danebell.collab

import TestUtils._
import org.scalatest.{FlatSpec, Matchers}

class TestEntities extends FlatSpec with Matchers {
  "CollabSystem" should "find personal names" in {
    val ms = system.extract("Councilwoman Davis proposed an amendment to the motion.")
    ms.filter(_ matches "Person").map(_.text) should contain ("Councilwoman Davis")
    val es = ms.head.document.sentences.head.entities
    es should not be empty
    es.get(0) should be ("B-Title")
    es.get(1) should be ("PERSON")
  }
}