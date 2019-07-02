package com.github.danebell.collab

import TestUtils._
import org.scalatest.{FlatSpec, Matchers}

class TestEvents extends FlatSpec with Matchers {
//  "CollabSystem" should "find proposals" in {
//    val ms = system.extract("Councilmember Bradford made a motion to enter into Task Order No.17 with Sweitzer Engineering, Inc. for the necessary design phase documents, erosion control plans and permit applications necessary for the replacement of the aging sewage force main from the Shattuck Industrial Blvd Lift Station to the north end of the airport.")
//    val less = ms filter (_ matches "Propose")
//    less should not be empty
//  }

  "CollabSystem" should "find consultation" in {
    val s = "Informal consultation between Golden Pass and the DOT regarding additional LNG and pipeline safety and federal safety standards is currently ongoing."
    val ms = system.extract(s)
    val less = ms filter (_ matches "Consultation")
    less should not be empty
  }

  it should "find consultation" in {
    val s = "BOEM is acting as lead agency in the reinitiated consultation, with BSEE involvement."
    val ms = system.extract(s)
    val less = ms filter (_ matches "Consultation")
    less should not be empty
  }

}