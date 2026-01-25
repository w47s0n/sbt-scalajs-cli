package com.w47s0n.scalajscli.util

import scala.sys.process._
import java.nio.file.{Paths, Files}
import com.w47s0n.consolebox.Consolebox
import com.w47s0n.consolebox.*
import scala.util.matching.Regex
import LoggingUtils._
import java.nio.file.Path
import com.w47s0n.scalajscli.util.ScalaJSCodeWatcher.CompanionProcess
import scala.tools.util.PathResolver.AsLines


object CLIHelper {
  private val DevServerUrl = "http://localhost:9876"

  case class FastOptJS(sourceDirs: List[Path], compilationRunner: () => Boolean)

  /* npm, bun, yan.... */
  case class JSDevServer(
    runCommand: Cmd,
    installPackagesCommand: Cmd,
    startupMessage: String, 
    successMessage: String,
  )

  case class Cmd(command: String, successPattern: Regex)

  def boxedConfigs(configs: (String, String)*): String = {
    val formattedLines = configs.map { case (key, value) => s"$key: $value" }
    Consolebox.box(formattedLines.mkString("\n"))
  }

  private def installPackages(cmd: Cmd): Unit = {
    val successMessage = boxedSuccess("Installed packages successfully !")
    CommandWatcher.watch(cmd.command.split(" "), cmd.successPattern, successMessage)
  }

  private def removeDistFolderIfAny(prefix: String = ""): Unit = {
    val distFolder = Paths.get(s"${prefix}dist")
    if (Files.exists(distFolder)) {
      s"rm -r ${prefix}dist".!
      logWarning(s"Deleted existing '${prefix}dist' folder")
    }
  }

  def buildFrontend(prefix: String = "", backendUrl: Option[String] = None): Unit = {
    // installPackages(prefix)
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

  def startDevEnvironment(fastOptJSTask: FastOptJS, js: JSDevServer): Unit = {
    installPackages(js.installPackagesCommand)
    val toolName = js.runCommand.command.split(" ").head
    println(Consolebox.box(js.startupMessage))
    val viteServerStarter = () => {
      var viteReadyCalled = false
      val viteLogger = ProcessLogger(
        line => {
          if (js.runCommand.successPattern.findFirstIn(line).isDefined && !viteReadyCalled) {
            viteReadyCalled = true
            logSuccess(js.successMessage)
          }
          println(s"[$toolName] $line")
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
            logError(s"[toolName] $line")
          } else {
            println(s"[toolName] $line")
          }
        }
      )

      Process(js.runCommand.command,
        None,
        "FORCE_COLOR" -> "1"
      ).run(viteLogger)
    }

    new ScalaJSCodeWatcher(fastOptJSTask, CompanionProcess(toolName, viteServerStarter)).start()
  }
}
