package com.w47s0n.scalajscli.util

import com.w47s0n.consolebox.Consolebox
import com.w47s0n.consolebox.*
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.io.{File, FileWriter, PrintWriter}

/**
 * Shared logging utilities for development environment tools.
 * Provides consistent boxed message formatting across CLI and code watcher components.
 */
object LoggingUtils {

  // ============================================================================
  // Boxed Message Builders (return String)
  // ============================================================================

  def boxedSuccess(lines: String*): String =
    Consolebox.box(lines.mkString("\n"), LogLevel.Success)

  def boxedError(lines: String*): String =
    Consolebox.box(lines.mkString("\n"), LogLevel.Error)

  def boxedWarning(lines: String*): String =
    Consolebox.box(lines.mkString("\n"), LogLevel.Warning)

  def boxedInfo(lines: String*): String =
    Consolebox.box(lines.mkString("\n"))

  // ============================================================================
  // Log file (tee of stdout)
  // ============================================================================

  // ponytail: one global log per sbt JVM; `dev` blocks so only one runs at a time
  @volatile private var logWriter: Option[PrintWriter] = None

  def openLogFile(f: File): Unit = {
    f.getParentFile.mkdirs()
    logWriter.foreach(_.close())
    logWriter = Some(new PrintWriter(new FileWriter(f, false), true))
  }

  def stripAnsi(text: String): String =
    text.replaceAll("\u001B\\[[;\\d]*m", "")

  def out(line: String): Unit = synchronized {
    println(line)
    logWriter.foreach(_.println(stripAnsi(line)))
  }

  // ============================================================================
  // Direct Logging Helpers (print to stdout)
  // ============================================================================

  def logSuccess(lines: String*): Unit =
    out(boxedSuccess(lines: _*))

  def logError(lines: String*): Unit =
    out(boxedError(lines: _*))

  def logWarning(lines: String*): Unit =
    out(boxedWarning(lines: _*))

  def logInfo(lines: String*): Unit =
    out(boxedInfo(lines: _*))

  // ============================================================================
  // Utility Methods
  // ============================================================================

  def currentTimeFormatted(): String =
    LocalTime.now().format(DateTimeFormatter.ofPattern("h:mm a"))

  def currentTimeFormattedWithSeconds(): String =
    LocalTime.now().format(DateTimeFormatter.ofPattern("h:mm:ss a"))

  // ANSI color codes for styled logging
  private val CYAN = "\u001B[36m"
  private val GREEN = "\u001B[32m"
  private val RED = "\u001B[31m"
  private val RESET = "\u001B[0m"

  def logScalaJsInfo(message: String): Unit = {
    val timestamp = s"$CYAN${currentTimeFormattedWithSeconds()}$RESET"
    out(s"${GREEN}[Scala.js]$RESET $timestamp $message")
  }

  def logScalaJsError(message: String): Unit = {
    val timestamp = s"$CYAN${currentTimeFormattedWithSeconds()}$RESET"
    out(s"${RED}[Scala.js ERROR]$RESET $timestamp $message")
  }
}
