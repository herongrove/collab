package com.github.danebell.protests

import java.io.File

import scala.collection.immutable.ListMap
import jline.console.ConsoleReader
import jline.console.history.FileHistory

/**
  * Interactive shell
  */
object ProtestShell extends App {

  val reader = new ConsoleReader()
  val history = new FileHistory(new File(System.getProperty("user.home"), ".parentshellhistory"))

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

    val ieSystem = new ProtestSystem()

    println("\nWelcome to the Protests Shell!")
    printCommands()

    var running = true

    while (running) {
      val line = reader.readLine
      line match {
        case ":help" =>
          printCommands()

        case ":reload" =>
          ieSystem.reload()

        case ":exit" | null =>
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

      // preprocessing
      val doc = ieSystem.annotate(text)

      // extract mentions from annotated document
      val mentions = ieSystem.extractFrom(doc).sortBy(m => (m.sentence, m.getClass.getSimpleName))

      // debug display the mentions
      displayMentions(mentions, doc, true)
    }

}
