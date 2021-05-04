package com.github.danebell.collab

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import org.apache.commons.io.FilenameUtils
import com.github.danebell.collab.utils.FileUtils._

object SummarizeDocuments extends App with LazyLogging {
  val config = ConfigFactory.load()

  val proc = new CollabProcessor()
  val nonce = proc.mkDocument("consulted")

  val papersDir = config.getString("collab.clean")
  val outFile = config.getString("collab.docInfo")

  val papers = listFilesRecursively(papersDir)

  val lines = papers.par.map { file =>
    val txt = getTextFromFile(file)
    if (txt.trim.nonEmpty) {
      val doc = proc.mkDocument(txt, keepText = false)
      doc.id = Option(FilenameUtils.getBaseName(file.getName))
      val wordTotal = doc.sentences.map(_.words.length).sum.toString
      val distinctWords = doc.sentences.flatMap(_.words).distinct.length
      Seq(
        doc.id.getOrElse("UNKNOWN"),
        wordTotal.toString,
        distinctWords.toString
      )
    } else Nil
  }

  val header = "docID\ttotalWords\tdistinctWords\n"
  val summary = header + lines.filterNot(_.isEmpty).map(_.mkString("\t")).mkString("\n")

  writeTextToFile(outFile, summary)
}