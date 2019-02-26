package com.github.danebell.collab.ner

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import org.clulab.processors.clu.tokenizer.Tokenizer
import org.clulab.utils.ScienceUtils
import java.io._
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.zip.{GZIPInputStream, GZIPOutputStream}

import com.github.danebell.collab.CollabProcessor

/**
  * Representation of a knowledge base and its corresponding taxonomic label.
  * @param kbName Unique name of a knowledge base file (minus its file extension)
  * @param neLabel The taxonomic label this knowledge base file should be added to
  */
case class KbEntry(kbName: String, neLabel: String)

/**
  * Parent class for classes that produce taxonomically defined dictionaries from knowledge bases.
  */
class KbGenerator { }

/**
  * [[KbGenerator]] for collab.
  * @param config Configuration file for input-output locations
  */
class CoKbGenerator(val config: Config = ConfigFactory.load())
  extends KbGenerator with LazyLogging {

  /** Minimal processor, used solely for the tokenization of resources */
  lazy val tokenizer: Tokenizer = (new CollabProcessor).tokenizer

  /** Utility for normalizing unicode characters */
  val su = new ScienceUtils

  /** Stoplist of terms that should never match in NER */
  val stopWords: Set[String] = Set() //(new CoNerPostProcessor).stopWords

  /**
    * Returns the input [[String]] with ascii-only characters
    */
  def normalizeTerm(line: String): String = su.replaceUnicodeWithAscii(line)

  /**
    * Tokenizes a resource line with CollabProcessor. </br>
    * This is important! We must guarantee that KB text is processed similarly to raw text!
    * @param line The KB line
    * @return The tokenized line
    */
  def tokenizeResourceLine(line: String): Array[String] = {
    tokenizer
      .tokenize(line, sentenceSplit = false) // Array[Sentence]
      .headOption                            // Option[Sentence]
      .map(_.words)                          // Option[Array[String]]
      .getOrElse(Array[String]())            // Array[String]
  }

  /**
    * Returns a file location for the input to a KB -> dictionary conversion
    * @param entry The KbEntry being converted
    * @param inputDir The location of the input kb
    */
  def mkInputFile(entry: KbEntry, inputDir: String): String = {
    val base = inputDir + File.separator + entry.kbName + ".tsv"
    // FIXME: this is dirty
    if (new File(base).exists()) base else base + ".gz"
  }

  /**
    * Returns a file location for the output of a KB -> dictionary conversion
    * @param entry The KbEntry being converted
    * @param outputDir The location for the output dictionaries
    */
  def mkOutputFile(entry: KbEntry, outputDir: String): String = {
    outputDir + File.separator + entry.neLabel + ".tsv.gz"
  }

  /**
    * Returns tokenized dictionary terms from a tab-separated knowledge base.
    * @param entry The KB to read from and what taxonomic label it corresponds to
    * @param inputDir The location of the KB
    */
  def convertKB(entry: KbEntry, inputDir: String, nameField: Int): Seq[String] = {
    // load KB file
    val reader = {
      val inputPath: String = mkInputFile(entry, inputDir)
      val inputStream = inputPath match {
        // handle compressed files
        case gz if gz endsWith "gz" => new GZIPInputStream(new FileInputStream(gz))
        case normalFile => new FileInputStream(normalFile)
      }
      scala.io.Source.fromInputStream(inputStream)
    }

    val outputLines = reader.getLines flatMap { line =>
      val trimmedLine = line.trim
      if(! trimmedLine.isEmpty && ! trimmedLine.startsWith("#")) { // ignore comments/blank
        // select
        val kbTokens = line.split("\t")
        val term = kbTokens(nameField)
        // normalize unicode characters
        val normalizedTerm = normalizeTerm(term)
        // tokenize using CollabProcessor
        val tokens = tokenizeResourceLine(normalizedTerm)
        val tokenized = tokens.mkString(" ")
        if (stopWords.contains(tokenized.toLowerCase)) {
          None
        } else {
          Option(tokenized)
        }
      } else { // #-comment or blank
        None
      }
    }

    val kbRows = outputLines
      .filter(_.nonEmpty) // in case some lines contain just \t
      .toList // Iterator -> List
      .sorted // easier debug and trie and .distinct
      .distinct // .distinct benefits from .sorted

    reader.close()

    kbRows
  }
}

/**
  * Produces taxonomic dictionaries for collab.
  */
object CoKbGenerator extends LazyLogging {
  val config: Config = ConfigFactory.load()

  // The index of the KB names in collab's KB config file
  val nameField = 0
  // The index of the taxonomic labels in collab's KB config file
  val labelField = 1

  // Performs the actual conversion
  val generator = new CoKbGenerator()

  /**
    * Produces taxonomic dictionaries for collab.
    * @param args All arguments are ignored. Refer to reference.conf.
    */
  def main (args: Array[String]) {
    // Find out which KBs are to be converted and to what labels
    val kbConfig = config.getString("collab.ner.kbConfig")
    val entries = loadConfig(kbConfig, nameField, labelField)

    // Find the file path of the resource directory
    val currentDir = new File(".").getCanonicalPath
    val resourceElements = Seq("src", "main", "resources")
    val resourceDir = (currentDir +: resourceElements).mkString(File.separator)

    val inDir = config.getString("collab.ner.kbRawDir")
    val inDirLoc = resourceDir + File.separator + inDir
    logger.info("Input directory: " + inDirLoc)

    val outDir = config.getString("collab.ner.kbNerDir")
    val outDirLoc = resourceDir + File.separator + outDir
    logger.info("Output directory: " + outDir)

    logger.info(s"Will convert a total of ${entries.size} KBs:")

    for(entry <- entries) {
      val outFile = generator.mkOutputFile(entry, outDirLoc)
      val previousOutput = new File(outFile)
      // delete the previous output
      if(previousOutput.exists()) {
        previousOutput.delete()
        logger.info(s"Deleted old output ${previousOutput.getAbsolutePath}.")
      }
    }
    for(entry <- entries) {
      logger.info(s"KB:${entry.kbName} to NE:${entry.neLabel}.")
      val outFile = generator.mkOutputFile(entry, outDirLoc)

      // get the KB's dictionary entries (tokenized)
      val lines = generator.convertKB(entry, inDirLoc, nameField)

      // append to output; we may have multiple KBs using the same taxonomic label
      val isFirst = ! new File(outFile).exists()
      val writer =
        new PrintWriter(
          new GZIPOutputStream(
            new FileOutputStream(outFile, true)))
      if(isFirst) writer.println(s"# Created by ${getClass.getName} on $now.")

      writer.print(lines.mkString("\n"))
      writer.println()
      writer.close()

      logger.info(s"Done. Read ${lines.length} lines from ${entry.kbName}")
    }
  }

  /**
    * Returns [[KbEntry]]s to be loaded from a tab-separated configuration file
    * @param configFile The location of the configuration file
    * @param nameField The index of KB name in configFile
    * @param labelField The index of the taxonomic label in configFile
    */
  def loadConfig(configFile: String, nameField: Int, labelField: Int): Seq[KbEntry] = {
    println(configFile)
    // load KB config file
    // val lines = Source.fromResource(configFile).getLines() map (_.trim) // Scala 2.12 and later
    val javastyle = File.separator + configFile
    val stream: InputStream = getClass.getResourceAsStream(javastyle)
    val lines = scala.io.Source.fromInputStream(stream).getLines map (_.trim)
    // ignore blank lines and #-comments
    val contentLines = lines filterNot (line => line.isEmpty || line.startsWith("#"))

    val kbEntries = contentLines map { line: String =>
      // each line must have at least 2 tab-separated values
      val tokens = line.split("\t")
      assert(tokens.length >= 2, line)
      val kbName = tokens(nameField)
      val neLabel = tokens(labelField)
      KbEntry(kbName, neLabel)
    }

    kbEntries.toSeq
  }

  /**
    * Returns a [[String]] representation of the current date and time when the function is called.
    */
  def now: String = {
    val dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
    val date = new Date()
    dateFormat.format(date)
  }
}
