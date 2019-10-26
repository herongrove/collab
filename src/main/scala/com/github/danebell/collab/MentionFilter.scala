package com.github.danebell.collab

import com.github.danebell.collab.mentions.CollabMention
import org.clulab.odin.{EventMention, Mention, RelationMention, TextBoundMention}
import org.clulab.struct.Interval

object MentionFilter {


  /**
    * Returns the same mentions, filtering out repeats of events with the same arguments (preferring
    * [[EventMention]]s over [[RelationMention]]s.
    */
  def keepFirst(ms: Seq[Mention]): Seq[Mention] = {
    val (tbms, events) = ms.partition(_.isInstanceOf[TextBoundMention])
    val sameArgs = events.groupBy{ m => (m.label, m.arguments) }.values
    tbms ++ sameArgs.map{ group =>
      group.maxBy(_.isInstanceOf[EventMention])
    }
  }

  /**
    * Returns the same mentions, filtering out mentions whose arguments overlap. For example,
    * "John consulted daily" should never link John to John.
    */
  def nonIdenticalArgs(ms: Seq[Mention]): Seq[Mention] = {
    ms.filterNot{ m =>
      val argList = m.arguments.values.flatten.toList
      val argSet = argList.toSet
      // same mention used twice
      argList.length > argSet.size ||
        // different mentions with overlapping intervals
        argSet.exists{ a1 =>
          (argSet - a1).exists{ a2 =>
            a1.tokenInterval overlaps a2.tokenInterval
          }
        }
      }
  }

  /**
    * Returns the same mentions, keeping mentions with more specific (e.g., Organization) arguments
    * over less specific (e.g., NounPhrase) arguments when available.
    */
  def keepMostSpecific(ms: Seq[Mention]): Seq[Mention] = {
    val (tbms, ems) = ms.partition(_.isInstanceOf[TextBoundMention])
    // keep track of what mentions' arguments overlap (with the same arg label)
    val emArgIntervals: Seq[(Mention, Map[String, Seq[Interval]])] = ems.map{ m =>
      m -> m.arguments.map{ case (lbl, args) =>
        lbl -> args.map(_.tokenInterval)
      }
    }
    val mostSpecific = for {
      (m, intervals) <- emArgIntervals
      competitors = emArgIntervals.filter{ other => isOverlapping(intervals, other._2) }.map(_._1)
      //_ = println(s"==========\nMAIN: ${briefDesc(m)}\n==========\n${competitors.map(briefDesc).mkString("\n")}\n==========\n")
      if isMostSpecific(m, competitors)
    } yield m
    tbms ++ mostSpecific
  }

  def briefDesc(m: Mention): String = {
    val arguments = m.arguments.flatMap{ case (lbl, args) =>
        args.map(arg => s"""$lbl = "${arg.text}"""")
    }
    s"${m.label} (${arguments.mkString(", ")})"
  }

  /**
    * Returns true if for every argument in a, there's an argument in b with the same label that
    * overlaps it, and vice versa.
    * NB: inefficient
    */
  def isOverlapping(a: Map[String, Seq[Interval]], b: Map[String, Seq[Interval]]): Boolean = {
    if(a.keys.toSet != b.keys.toSet) return false

    val aAccountedFor = a.forall{ case (aLbl, aIntervals) =>
      aIntervals.forall{ aInterval =>
        b.exists{ case (bLbl, bIntervals) =>
          aLbl == bLbl && bIntervals.exists(_ overlaps aInterval)
        }
      }
    }
    val bAccountedFor = b.forall{ case (bLbl, bIntervals) =>
      bIntervals.forall{ bInterval =>
        a.exists{ case (aLbl, aIntervals) =>
          bLbl == aLbl && aIntervals.exists(_ overlaps bInterval)
        }
      }
    }

    aAccountedFor && bAccountedFor
  }

  def isMostSpecific(mention: Mention, competitors: Seq[Mention]): Boolean = {
    competitors.forall{ competitor =>
      isMoreSpecific(mention.arguments, competitor.arguments)
    }
  }

  def isMoreSpecific(a: Map[String, Seq[Mention]], b: Map[String, Seq[Mention]]): Boolean = {
    (a.keys ++ b.keys).forall{ lbl =>
      a.getOrElse(lbl, Nil).forall{ am =>
        b
          .getOrElse(lbl, Nil)
          .filter(_.tokenInterval overlaps am.tokenInterval)
          .forall{ bm =>
            isMoreSpecific(am.label, bm.label)
          }
      }
    }
  }

  def isMoreSpecific(a: String, b: String): Boolean = {
    val priority1 = Set("Organization", "Person", "Government")
    val priority2 = Set("ProperNoun", "FirstPerson")
    val priority3 = Set("NounPhrase")
    val priority4 = Set("VerbPhrase", "PrepPhrase", "WhPhrase", "SentencePhrase")

    val res = (a, b) match {
      case same if a == b => true
      case highest if priority1 contains a => true
      case (p2, p34) if priority2.contains(p2) && ! priority1.contains(p34) => true
      case ("NounPhrase", x) if priority4 contains x => true
      case _ => false
    }
    //println(s"compared $a to $b: $res")
    res
  }

}