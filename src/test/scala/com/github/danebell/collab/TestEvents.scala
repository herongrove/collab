package com.github.danebell.collab

import TestUtils._
import org.scalatest.{FlatSpec, Matchers}

class TestEvents extends FlatSpec with Matchers {
//  "CollabSystem" should "find proposals" in {
//    val ms = system.extract("Councilmember Bradford made a motion to enter into Task Order No.17 with Sweitzer Engineering, Inc. for the necessary design phase documents, erosion control plans and permit applications necessary for the replacement of the aging sewage force main from the Shattuck Industrial Blvd Lift Station to the north end of the airport.")
//    val less = ms filter (_ matches "Propose")
//    less should not be empty
//  }

//  "CollabSystem" should "find consultation in 'consultation between X and Y'" in {
//    val s = "Informal consultation between Golden Pass and the DOT regarding additional LNG and pipeline safety and federal safety standards is currently ongoing."
//    val ms = getMentions(s)
//    val less = ms filter (_ matches "Consultation")
//    less should not be empty
//  }
//
//  it should "find consultation in 'X is acting in consultation with Y involvement'" in {
//    val s = "BOEM is acting as lead agency in the reinitiated consultation, with BSEE involvement."
//    val ms = getMentions(s)
//    val less = ms filter (_ matches "Consultation")
//    less should not be empty
//  }
//
//  it should "find consultation in 'following consultation with X, Y ...'" in {
//    val s = "Following consultation with the Commandant of the U.S. Coast Guard and the Secretary of the Interior, USEPA assumed air quality responsibility for the OCS waters"
//    val ms = getMentions(s)
//    val less = ms filter (_ matches "Consultation")
//    less should not be empty
//  }

  "CollabSystem" should "find collaboration in 'a team of X and Y'" in {
    val s = "With   this   information,   a   team   of   resource   professionals,   including representatives  from  the  Cape  Fear  River  Watch,  New  Hanover  County  and  the  City  of  Wilmington,  conducted  a  follow-up  field investigation of the identified degradation issues."
    val ms = getMentions(s)
    val less = ms filter (_ matches "Collaboration")
    less should have length (3)
  }

  it should "find collaboration in 'participants represented X and Y'" in {
    val s = "Participants represented the National Drought Mitigation Center and NIDIS."
    val ms = getMentions(s)
    val less = ms filter (_ matches "Collaboration")
    less should have length (1)
  }

  it should "find collaboration in 'X worked with Y'" in {
    val s = "DCM  is  also working with  FEMA, the NSF, and Governor  Kilgrave"
    val ms = getMentions(s)
    val less = ms filter (_ matches "Collaboration")
    less should have length (3)
  }

  it should "find collaboration in 'X and Y worked on Z'" in {
    val s = "DCM  and FEMA are working together to kill the moon"
    val ms = getMentions(s)
    val less = ms filter (_ matches "Collaboration")
    less should have length (1)
  }
}