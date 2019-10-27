package com.github.danebell.collab.korrect

import java.io.File
import java.nio.file.Paths

import com.typesafe.config.{Config, ConfigFactory}
import com.github.danebell.collab.utils.FileUtils

class KorrectDocuments(val config: Config) {
//  val dict: Set[String] = FileUtils.getTextFromResource(config.getString("collab.dict"))
//    .split("\n+")
//    .map(_.trim.toLowerCase)
//    .toSet

  def subChars(text: String): String = {
    text
      .replaceAll("™", "'")
      .replaceAll("í", "'")
      .replaceAll("ﬂ|ﬁ", "\"")
      .replaceAll("Œ", "-")
      .replaceAll("Ł", "")
      .replaceAll("\uF0B7", "")
      .replaceAll("˜", "Th")
      .replaceAll("(?<![0-9])˚", "fi")
      .replaceAll("˝", "fl")
      .replaceAll("˙", "ff")
      .replaceAll("•", " ")
      .replaceAll("\uF07D", " ")
      .replaceAll("\uF0A7", " ")
      .replaceAll("˛", "")
      .replaceAll("\\|", "")
      .replaceAll("[⁰¹²³⁴⁵⁶⁷⁸⁹]+", "")
      .replaceAll("\\d{10,}", "<BIG_NUMBER>")
  }

  def cutHeaders(text: String): String =
    text.replaceAll("(?i)page\\s+\\d+(\\s*(of)|/\\s*\\d+|\\s+cont(inue|')d)?", "")

  def cutLinks(text: String): String = {
    text
      // image links
      .replaceAll("(\\* +)?!\\[([^\\]]*)\\]\\([^)]*\\)", "")
      // text links
      .replaceAll("\\[([^\\]]*)\\]\\([^)]*\\)", "$1")
      // bold
      .replaceAll("\\*\\*([^*]+)\\*\\*", "$1")
      // italics
      .replaceAll("_([^_]+)_", "$1")
      // remaining square brackets
      .replaceAll("[\\[\\]]", "")
      // headers and bullets
      .replaceAll("(?<=\n) *[#*]+", "")
      // headers and bullets
      .replaceAll("^ *[#*]+", "")
  }

  def addSpace(text: String): String =
    text
      .replaceAll("([A-Za-z][!?:\\.”]*)(\\d)", "$1 $2")
      .replaceAll("(\\d)([A-Za-z])", "$1 $2")
      .replaceAll("([a-z])([A-Z])", "$1 $2")
      .replaceAll("X([A-Z])", "$1")
      .replaceAll("([!?:\\.”])([A-Za-z])", "$1 $2")
      .replaceAll("([A-Za-z])/([A-Za-z])", "$1 / $2")

  def removeNewlines(text: String): String = {
    text.replaceAll("(?<=\n)([^ #*\n][^\r\n]+)\r?\n", "$1 ")
  }

  def addNewlines(text: String): String =
    text//.replaceAll("(?<!\\.) *(\\d+\\))", "\n$1")
      .replaceAll(" *• *", "\n")
      .replaceAll(" +o  ", "\n")
      .replaceAll("   +", "\n")
      .replaceAll("\f", "\n")

  def joinLines(text: String): String = {
    /*    def joinL(a: Seq[Seq[String]], b: Seq[String]): Seq[Seq[String]] = {
      val r1 = a.last.slice(0, a.last.length - 1)
      val r2 = r1 :+ (a.last.last + b.head)
      val r3 = r2 ++ b.tail
      a.slice(0, a.length - 1) :+ r3
    }

    def joinable(first: String, second: String): Boolean =
      (! dict.contains(first.toLowerCase) ||
        ! dict.contains(second.toLowerCase)) &&
        dict.contains((first + second).toLowerCase)


    def removeTrailing(str: String): String = str.replaceAll("\\p{Punct}+$", "")

    val notTitle = "([a-z]+|[A-Z]+)".r
    val title = "([A-Z][a-z]+)".r
    val lines = text.split("""\n""").map(_.trim.split("""\s+""").toSeq).toSeq

    val korrected = lines.foldLeft(Seq[Seq[String]]()) { case (r, c) =>
      if (r.isEmpty) {
        Seq(c)
      } else if (c.isEmpty) {
        r
      } else if (r.last.isEmpty) {
        r :+ c
      } else { try {
        (removeTrailing(r.last.last), removeTrailing(c.head)) match {
          case (_, title(_)) => r :+ c
          case (fine, alsoFine) if dict.contains(fine) && dict.contains(alsoFine) => r :+ c
          case (first, notTitle(second)) if joinable(first, second) => joinL(r, c)
          case _ => r :+ c
        }
      } catch { case e: Exception =>
        println("First:\t" + r.last.mkString(" "))
        println("Second:\t" + c.mkString(" "))
        println()
        r
      }
      }
    }

    korrected.map(_.mkString(" ")).mkString("\n")
  }
*/
    val noHeaders = cutHeaders(text)
    val goodChars = subChars(noHeaders)
    val noNL = removeNewlines(goodChars)
    val noLinks = cutLinks(noNL)
    val extraSpaces = addSpace(noLinks)
    val extraNewlines = addNewlines(extraSpaces)

    extraNewlines.replaceAll("  +", " ")
  }
}

object KorrectDocuments extends App {
  def apply(): KorrectDocuments = {
    val conf = ConfigFactory.load("reference.conf")
    new KorrectDocuments(conf)
  }

  def cleanEach(in: File, out: File): Unit = {
    val rawFiles = in.listFiles() filter (! _.isDirectory) filter (! _.getName.startsWith("."))
    val raw = rawFiles map (f => f.getName -> FileUtils.getTextFromFile(f))

    val clean = raw map { case (fn, text) => fn -> kd.joinLines(text) }

    clean foreach { case (fn, text) =>
      // println(s"File: $fn")
      FileUtils.writeTextToFile(fn, text, dir=out.getAbsolutePath)
    }

    val rawDirs = in.listFiles() filter (_.isDirectory)
    rawDirs foreach { inDir =>
      // println(s"Directory: ${inDir.getName} (${Paths.get(out.getAbsolutePath, inDir.getName)})")
      cleanEach(inDir, Paths.get(out.getAbsolutePath, inDir.getName).toFile)
    }
  }

  val conf = ConfigFactory.load("reference.conf")
  val kd = new KorrectDocuments(conf)

  val rawLoc = new File(conf.getString("collab.raw"))
  val cleanLoc = new File(conf.getString("collab.clean"))

  cleanEach(rawLoc, cleanLoc)
}