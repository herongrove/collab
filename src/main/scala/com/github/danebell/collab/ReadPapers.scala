package com.github.danebell.collab

import java.nio.file.Paths

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import org.apache.commons.io.FilenameUtils
import com.github.danebell.collab.utils.FileUtils._
import com.github.danebell.collab.utils.ExportUtils._
import org.clulab.odin.TextBoundMention

object ReadPapers extends App with LazyLogging {
  val config = ConfigFactory.load()

  val system = new CollabSystem()
  val nonce = system.proc.mkDocument("consulted")
  system.proc.annotate(nonce)
  system.extract(nonce)

  val papersDir = config.getString("collab.clean")
  val outDir = config.getString("collab.mentions")

  val alreadyDone = listFilesRecursively(outDir).map(_.getName)
  val papers = listFilesRecursively(papersDir).filterNot{ f => alreadyDone.contains(f.getName) }

  papers.par.foreach { file =>
    val txt = getTextFromFile(file)
    val lines = if (txt.trim.nonEmpty & txt.matches()) {
      val doc = system.annotate(txt, keepText = false)
      doc.id = Option(FilenameUtils.getBaseName(file.getName))
      val mentions = system.extract(doc) filterNot (_.isInstanceOf[TextBoundMention])
      logger.debug(s"${mentions.length} event mentions in $file")
      mentions.map{ m => mentionToTabular(m, "actor1", "actor2") }
    } else Nil
    val outFile = Paths.get(outDir, file.getName).toFile
    writeTextToFile(outFile, lines.mkString("\n"))
  }
}