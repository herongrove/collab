package com.github.danebell.collab

import com.github.danebell.collab.mentions.CollabMention
import org.clulab.odin.{EventMention, Mention, RelationMention, TextBoundMention}
import org.clulab.struct.Interval

object MentionFilter {


  /**
    * Returns the same mentions, filtering out entities that are only good for building other
    * entities.
    */
  def keepNpEntities(ms: Seq[Mention]): Seq[Mention] = {
    val disallowed = Set("VerbPhrase", "PrepPhrase", "WhPhrase", "SentencePhrase")
    ms.filterNot{ m => disallowed contains m.label }
  }

  /**
    * Returns the same mentions, filtering out repeats of events with the same arguments (preferring
    * [[EventMention]]s over [[RelationMention]]s.
    */
  def keepFirst(ms: Seq[Mention]): Seq[Mention] = {
    val (tbms, events) = ms.partition(_.isInstanceOf[TextBoundMention])
    val sameArgs = events.groupBy{ m => (m.sentence, m.label, m.arguments) }.values
    tbms ++ sameArgs.map{ group =>
      group.maxBy(_.isInstanceOf[EventMention])
    }
  }

  /**
    * Returns true if the argument [[Mention]]'s token [[Interval]] overlaps with that of the mention
    * [[Mention]]'s trigger
    */
  def overlapsTrigger(mention: Mention, argument: Mention): Boolean = {
    mention.isInstanceOf[EventMention] &&
      mention.asInstanceOf[EventMention]
        .trigger
        .tokenInterval
        .overlaps(argument.tokenInterval)
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
        // any mentions with overlapping intervals?
        argSet.exists{ a1 =>
          // can't overlap with trigger either
          overlapsTrigger(m, a1) ||
          // any mention that overlaps with this one?
          (argSet - a1).exists{ a2 =>
            (a1.tokenInterval overlaps a2.tokenInterval) ||
              // or do they have the same text exactly?
              (a1.text == a2.text)
          }
        }
      }
  }

  /**
    * Returns the same mentions, keeping mentions with more specific (e.g., Organization) labels
    * over less specific (e.g., NounPhrase) labels when available.
    */
  def keepMostSpecific(ms: Seq[Mention]): Seq[Mention] = {
    // apply to TextBoundMentions (there should be no non-TBMs at this time, so ignore any)
    val (tbms, ems) = ms.partition(_.isInstanceOf[TextBoundMention])
    // keep track of what mentions' intervals overlap
    val intervals = tbms.map{ m => (m, m.tokenInterval) }.toMap
    // for each mention, keep only if it's the most specifically labeled among its overlappers
    val mostSpecific = for {
      (m, interval) <- intervals
      competitors = (intervals - m).filter{ other =>
        other._1.sentence == m.sentence & interval.overlaps(other._2)
      }.keys.toSeq
      //_ = println(s"==========\nMAIN: ${briefDesc(m)}\n==========\n${competitors.map(briefDesc).mkString("\n")}\n==========\n")
      if isMostSpecific(m.label, competitors.map(_.label))
    } yield m
    mostSpecific.toSeq ++ ems
  }

//  def briefDesc(m: Mention): String = {
//    val arguments = m.arguments.flatMap{ case (lbl, args) =>
//        args.map(arg => s"""$lbl = "${arg.text}"""")
//    }
//    s"${m.label} (${arguments.mkString(", ")})"
//  }

//  /**
//    * Returns true if for every argument in a, there's an argument in b with the same label that
//    * overlaps it, and vice versa.
//    * NB: inefficient
//    */
//  def isOverlapping(a: Map[String, Seq[Interval]], b: Map[String, Seq[Interval]]): Boolean = {
//    if(a.keys.toSet != b.keys.toSet) return false
//
//    val aAccountedFor = a.forall{ case (aLbl, aIntervals) =>
//      aIntervals.forall{ aInterval =>
//        b.exists{ case (bLbl, bIntervals) =>
//          aLbl == bLbl && bIntervals.exists(_ overlaps aInterval)
//        }
//      }
//    }
//    val bAccountedFor = b.forall{ case (bLbl, bIntervals) =>
//      bIntervals.forall{ bInterval =>
//        a.exists{ case (aLbl, aIntervals) =>
//          bLbl == aLbl && aIntervals.exists(_ overlaps bInterval)
//        }
//      }
//    }
//
//    aAccountedFor && bAccountedFor
//  }

//  def isMostSpecific(mention: Mention, competitors: Seq[Mention]): Boolean = {
//    competitors.forall{ competitor =>
//      isMoreSpecific(mention.arguments, competitor.arguments)
//    }
//  }
//
//  def isMoreSpecific(a: Map[String, Seq[Mention]], b: Map[String, Seq[Mention]]): Boolean = {
//    (a.keys ++ b.keys).forall{ lbl =>
//      a.getOrElse(lbl, Nil).forall{ am =>
//        b
//          .getOrElse(lbl, Nil)
//          .filter(_.tokenInterval overlaps am.tokenInterval)
//          .forall{ bm =>
//            isMoreSpecific(am.label, bm.label)
//          }
//      }
//    }
//  }

  def isMostSpecific(mentionLabel: String, competitors: Seq[String]): Boolean = {
    competitors.forall{ c => isMoreSpecific(mentionLabel, c) }
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

  def keepMultipleArgs(ms: Seq[Mention]): Seq[Mention] = {
    val (tbms, ems) = ms.partition(_.isInstanceOf[TextBoundMention])
    val emsMultipleArgs = ems.filter{ m => m.arguments.values.flatten.size > 1 }
    tbms ++ emsMultipleArgs
  }
}