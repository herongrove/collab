package com.github.danebell.collab.utils

import java.io.{File, FileNotFoundException, PrintWriter}
import java.nio.charset.StandardCharsets
import java.nio.file.Paths

import com.typesafe.scalalogging.LazyLogging

import scala.io.BufferedSource
import scala.io.Source

object FileUtils extends LazyLogging {
  val utf8 = StandardCharsets.UTF_8.toString

  def listFiles(dir: String): Seq[File] = {
    listFiles(new File(dir))
  }

  def listFiles(dir: File): Seq[File] = {
    if (dir.exists && dir.isDirectory) {
      dir.listFiles.filter(_.isFile).toList
    } else {
      Nil
    }
  }

  def listFilesRecursively(dir: String): Seq[File] = {
    listFiles(new File(dir))
  }

  def listFilesRecursively(dir: File): Seq[File] = {
    if (dir.exists && dir.isDirectory) {
      val (files, dirs) = dir.listFiles().partition(_.isFile)
      files ++ dirs.filter(_.isDirectory).flatMap(listFilesRecursively)
    } else {
      Nil
    }
  }

  def sourceFromResource(path: String): BufferedSource = {
    val url = FileUtils.getClass.getResource(path)

    if (url == null)
      throw newFileNotFoundException(path)
    logger.info("Sourcing resource " + url.getPath())
    Source.fromURL(url, utf8)
  }

  def sourceFromFile(file: File): BufferedSource = {
    logger.info("Sourcing file " + file.getPath())
    Source.fromFile(file, utf8)
  }

  def sourceFromFile(path: String): BufferedSource = sourceFromFile(new File(path))

  def newFileNotFoundException(path: String): FileNotFoundException = {
    val message1 = path + " (The system cannot find the path specified"
    val message2 = message1 + (if (path.startsWith("~")) ".  Make sure to not use the tilde (~) character in paths in lieu of the home directory." else "")
    val message3 = message2 + ")"

    new FileNotFoundException(message3)
  }

  protected def getTextFromSource(source: Source): String = {
    try {
      source.mkString
    }
    finally {
      source.close()
    }
  }

  def getTextFromResource(path: String): String =
    getTextFromSource(sourceFromResource(path))

  def getTextFromFile(file: File): String =
    getTextFromSource(sourceFromFile(file))

  def getTextFromFile(file: String): String = {
    val fileFile = new File(file)
    getTextFromSource(sourceFromFile(fileFile))
  }

  def writeTextToFile(file: String, text: String, dir: String = ""): Unit = {
    writeTextToFile(Paths.get(dir, file).toFile, text)
  }

  def writeTextToFile(file: File, text: String): Unit = {
    if (! file.getParentFile.exists) {
      file.getParentFile.mkdirs()
    }
    val p = new PrintWriter(file)
    try {
      p.write(text)
    }
    finally {
      p.close()
    }
  }
}