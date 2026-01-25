package com.w47s0n.scalajscli.util

import scala.sys.process._
import java.nio.file.{Paths, Files}
import com.w47s0n.consolebox.Consolebox
import com.w47s0n.consolebox.*
import scala.util.matching.Regex
import sbt._
import sbt.Keys._
import LoggingUtils._

object CLIHelper {
  private val DevServerUrl = "http://localhost:9876"

  def boxedConfigs(configs: (String, String)*): String = {
    val formattedLines = configs.map { case (key, value) => s"$key: $value" }
    Consolebox.box(formattedLines.mkString("\n"))
  }

  private def installPackages(prefix: String = ""): Unit = {
    val command = List("bun", "install") ++ (if (prefix.nonEmpty) Seq("--cwd", prefix) else Seq.empty)
    val pattern = "packages installed".r
    val successMessage = boxedSuccess("Installed packages successfully !")
    CommandWatcher.watch(command, pattern, successMessage)
  }

  private def removeDistFolderIfAny(prefix: String = ""): Unit = {
    val distFolder = Paths.get(s"${prefix}dist")
    if (Files.exists(distFolder)) {
      s"rm -r ${prefix}dist".!
      logWarning(s"Deleted existing '${prefix}dist' folder")
    }
  }

  def buildFrontend(prefix: String = "", backendUrl: Option[String] = None): Unit = {
    installPackages(prefix)
    val distPrefix = if (prefix.isEmpty) "" else s"$prefix/"
    removeDistFolderIfAny(distPrefix)

    val buildCommand = Seq("bun", "run", "build") ++ (if (prefix.nonEmpty) Seq("--cwd", prefix) else Seq.empty)
    val pattern = ".built in.".r

    val messages = Seq(
      s"Web app is available at directory '${distPrefix}dist'"
    ) ++ backendUrl.map(url => s"Backend URL of this build: $url") ++
      backendUrl.map(_ => "Tip: Change backend URL: BACKEND_BASE_URL=http://myserver.com sbt")

    val successMessage = boxedSuccess(messages: _*)
    CommandWatcher.watch(buildCommand, pattern, successMessage)
  }

  def runTauriCommand(command: String, prefix: String = ""): Unit = {
    val commandParts = command.split("\\s+")
    val tauriCommand = Seq("bun", "run", "tauri") ++ commandParts ++ (if (prefix.nonEmpty) Seq("--cwd", prefix) else Seq.empty)
    val exitCode = Process(tauriCommand).!
    if (exitCode != 0) {
      logError(s"Tauri command failed with exit code $exitCode")
      throw new RuntimeException(s"tauri $command failed")
    } else {
      logSuccess("Tauri app built successfully!")
    }
  }

  def startDevEnvironment(
    scalaVersion: String,
    sourceDirectories: Seq[File],
    compilationRunner: () => Boolean,
    devCommand: Seq[String],
    backendUrl: Option[String] = None
  ): Unit = {
    installPackages(prefix = "") // TODO:
    printStartupMessage(backendUrl)

    val viteServerStarter = () => {
      var viteReadyCalled = false
      val viteLogger = ProcessLogger(
        line => {
          if (line.contains("Running") && !viteReadyCalled) {
            viteReadyCalled = true
            val messages = Seq(
              "Development environment ready!",
              s"changeme - Web app now available on $DevServerUrl"
            ) ++ backendUrl.map(url => s"Backend URL: $url") ++ Seq(
              "",
              "Press Ctrl+C to stop"
            ) ++ backendUrl.map(_ => "Tip: Change backend URL: BACKEND_BASE_URL=http://myserver.com sbt")

            logSuccess(messages: _*)
          }
          println(s"[Tauri] $line")
        },
        line => {
          // Filter stderr: only treat actual errors as errors
          val lowerLine = line.toLowerCase
          val isActualError = lowerLine.contains("error:") ||
                              lowerLine.contains("panic") ||
                              lowerLine.contains("fatal") ||
                              lowerLine.contains("failed to") ||
                              (lowerLine.contains("error") && lowerLine.contains("code"))

          if (isActualError) {
            logError(s"[Tauri ERROR] $line")
          } else {
            println(s"[Tauri] $line")
          }
        }
      )

      // val devCommand = Seq("bun", "run", "tauri", "dev") ++ (if (prefix.nonEmpty) Seq("--cwd", prefix) else Seq.empty)
      Process(
        devCommand,
        None,
        "FORCE_COLOR" -> "1"
      ).run(viteLogger)
    }

    new ScalaJSCodeWatcher(
      sourceDirectories,
      compilationRunner,
      ScalaJSCodeWatcher.CompanionProcess("Tauri dev server", viteServerStarter)
    ).start()
  }

  private def printStartupMessage(backendUrl: Option[String]): Unit = {
    val configs = Seq(
      "Starting development environment" -> ""
    ) ++ backendUrl.map(url => "Backend URL" -> url) ++ Seq(
      "Scala.js compiler" -> "Starting in watch mode...",
      "Vite dev server" -> "Starting..."
    )

    println(boxedConfigs(configs: _*))
  }
}
