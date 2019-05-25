package com.github.danebell.collab

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import org.apache.commons.io.FilenameUtils

import com.github.danebell.collab.utils.FileUtils._
import com.github.danebell.collab.utils.ExportUtils._

object ReadPapers extends App with LazyLogging {
  val config = ConfigFactory.load()

  val system = new CollabSystem()

  val papersDir = config.getString("collab.clean")
  val outFile = config.getString("collab.out")

  val papers = listFilesRecursively(papersDir)
    .flatMap { file =>
      val txt = getTextFromFile(file)
      if (txt.trim.nonEmpty) {
        val doc = system.proc.mkDocument(txt, keepText = false)
        doc.id = Option(FilenameUtils.getBaseName(file.getName))
        system.proc.annotate(doc)
        Option(doc)
      } else {
        None
      }
    }

  for (paper <- papers.par) {
    val mentions = system.extract(paper)
    val lines = mentions map mentionToTabular
    writeTextToFile(outFile, lines.mkString("\n"))
  }
}