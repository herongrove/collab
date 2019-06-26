package com.github.danebell.collab.utils

import java.io.File
import java.nio.file.Paths

import com.github.danebell.collab.utils.FileUtils.{getTextFromFile, listFilesRecursively, writeTextToFile}
import com.typesafe.config.{Config, ConfigFactory}

object MergeOutput extends App {
  val config: Config = ConfigFactory.load()
  val dataDir: String = config.getString("collab.data")
  val outFile: File = Paths.get(dataDir, "mentions.tsv").toFile
  val mentionDir: String = config.getString("collab.mentions")
  val mentionFiles: Seq[String] = listFilesRecursively(mentionDir).map(_.getName)

  val header: String = "Event_Label\tArgument1_Text\tArgument1_Label\tArgument2_Text\tArgument1_Label\tFile\tRule\tText"

  val txts: Seq[String] = for {
    filename <- mentionFiles
    file = Paths.get(mentionDir, filename).toFile
    txt = getTextFromFile(file).trim
    if txt.nonEmpty
  } yield txt

  writeTextToFile(outFile, (header +: txts).mkString("\n"))
}