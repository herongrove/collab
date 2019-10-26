package com.github.danebell.collab

import java.io.File
import scala.collection.immutable.ListMap

import jline.console.ConsoleReader
import jline.console.history.FileHistory

import com.github.danebell.collab.utils.DisplayUtils._
import com.github.danebell.collab.korrect.KorrectDocuments

/**
  * Interactive shell
  */
object CollabShell extends App {

  val reader = new ConsoleReader()
  val history = new FileHistory(new File(System.getProperty("user.home"), ".collabshellhistory"))

  val prompt = ">>> "
  reader.setPrompt(prompt)
  reader.setHistory(history)
  sys addShutdownHook {
    reader.getTerminal.restore()
    reader.shutdown()
    history.flush() // flush file before exiting
  }

  val commands = ListMap(
    ":help" -> "show commands",
    ":reload" -> "reload grammar",
    ":exit" -> "exit system"
  )

  val ieSystem = new CollabSystem()
  val korrect = KorrectDocuments()

  val nonceDoc = ieSystem.annotate("consulted")
  val nonceMentions = ieSystem.extract(nonceDoc)

  println("\nWelcome to the Collab Shell!")
  printCommands()

  var running = true

  while (running) {
    val line = reader.readLine
    line match {
      case ":help" =>
        printCommands()

      case ":reload" =>
        ieSystem.reload()

      case ":exit" | ":quit" | null =>
        running = false

      case text =>
        extractFrom(text)
    }
  }

  // summarize available commands
  def printCommands(): Unit = {
    println("\nCOMMANDS:")
    for ((cmd, msg) <- commands)
      println(s"\t$cmd\t=> $msg")
    println()
  }

  def extractFrom(text:String): Unit = {
    val cleaned = korrect.joinLines(text)

    // preprocessing
    val doc = ieSystem.annotate(cleaned)

    // extract mentions from annotated document
    val mentions = ieSystem.extract(doc).sortBy(m => (m.sentence, m.getClass.getSimpleName))

    // debug display the mentions
    displayMentions(mentions, doc, printDeps=true)
  }

}
