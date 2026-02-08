package com.w47s0n.scalajscli

import sbt._
import sbt.Keys._
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import com.w47s0n.scalajscli.util.CLIHelper
import scala.util.matching.Regex

object ScalaJsCli extends AutoPlugin {

  // This plugin will automatically be enabled when ScalaJS plugin is enabled
  override def requires = ScalaJSPlugin
  override def trigger = allRequirements

  // Package manager abstraction
  sealed trait PackageManager {
    def binary: String
  }

  object PackageManager {
    case object Npm extends PackageManager { val binary = "npm" }
    case object Yarn extends PackageManager { val binary = "yarn" }
    case object Bun extends PackageManager { val binary = "bun" }
    case object Pnpm extends PackageManager { val binary = "pnpm" }
  }

  // Command with package manager awareness and working directory support
  case class Cmd(
    tool: PackageManager,
    subcommand: String,
    args: List[String],
    successPattern: Regex,
    workingDir: Option[File] = None
  ) {
    def command: String = {
      s"${tool.binary} $subcommand ${args.mkString(" ")}".trim
    }
  }

  object Cmd {
    // Smart constructors for each package manager
    object npm extends PackageManagerBuilder(PackageManager.Npm)
    object yarn extends PackageManagerBuilder(PackageManager.Yarn)
    object bun extends PackageManagerBuilder(PackageManager.Bun)
    object pnpm extends PackageManagerBuilder(PackageManager.Pnpm)
  }

  class PackageManagerBuilder(tool: PackageManager) {
    // Default success patterns
    private val installPattern = ".*added.*package.*".r
    private val devPattern = ".*ready.*".r
    private val buildPattern = ".*built.*".r

    class CmdBuilder private[PackageManagerBuilder](
      subcommand: String,
      args: List[String] = Nil,
      pattern: Regex = ".*".r,
      workDir: Option[File] = None
    ) {
      def withArgs(newArgs: String*): CmdBuilder =
        new CmdBuilder(subcommand, args ++ newArgs.toList, pattern, workDir)

      def withPrefix(path: String): CmdBuilder =
        new CmdBuilder(subcommand, args, pattern, Some(file(path)))

      def withWorkingDir(dir: File): CmdBuilder =
        new CmdBuilder(subcommand, args, pattern, Some(dir))

      def matching(regex: Regex): CmdBuilder =
        new CmdBuilder(subcommand, args, regex, workDir)

      // Convert to Cmd (explicit)
      def toCmd: Cmd = Cmd(tool, subcommand, args, pattern, workDir)
    }

    // Implicit conversion from CmdBuilder to Cmd
    implicit def cmdBuilderToCmd(builder: CmdBuilder): Cmd = builder.toCmd

    // Predefined commands with sensible defaults
    def install: CmdBuilder =
      new CmdBuilder("install", Nil, installPattern)

    def run(script: String): CmdBuilder =
      new CmdBuilder("run", List(script), devPattern)

    def add(packages: String*): CmdBuilder =
      new CmdBuilder(
        if (tool == PackageManager.Yarn) "add" else "install",
        packages.toList,
        installPattern
      )

    def dev: CmdBuilder =
      new CmdBuilder("run", List("dev"), devPattern)

    def build: CmdBuilder =
      new CmdBuilder("run", List("build"), buildPattern)

    def custom(subcommand: String, args: String*): CmdBuilder =
      new CmdBuilder(subcommand, args.toList)
  }

  case class DevConfig(
      command: Cmd,
      startupMessage: String,
      successMessage: String
  )

  case class BuildConfig(
      command: Cmd,
      startupMessage: String,
      successMessage: String
  )

  case class JSToolConfig(
      installPackagesCommand: Cmd,
      dev: DevConfig,
      build: BuildConfig
  )

  // Define custom settings and tasks
  object autoImport {
    val dev =
      taskKey[Unit]("Start dev mode with continuous Scala.js compilation")
    val publishDist =
      taskKey[Unit]("Build optimized Scala.js and bundle the JavaScript application")

    val jsTool = settingKey[JSToolConfig](
      "JavaScript tool configuration (install, dev, and build commands)"
    )

    // Export case classes for user convenience
    type Cmd = ScalaJsCli.Cmd
    val Cmd = ScalaJsCli.Cmd
    type PackageManager = ScalaJsCli.PackageManager
    val PackageManager = ScalaJsCli.PackageManager
    type DevConfig = ScalaJsCli.DevConfig
    val DevConfig = ScalaJsCli.DevConfig
    type BuildConfig = ScalaJsCli.BuildConfig
    val BuildConfig = ScalaJsCli.BuildConfig
    type JSToolConfig = ScalaJsCli.JSToolConfig
    val JSToolConfig = ScalaJsCli.JSToolConfig
  }

  import autoImport._

  // Default settings that will be automatically added to projects
  override lazy val projectSettings = Seq(
    publishDist := {
      jsTool.?.value match {
        case Some(tool) =>
          val _ = (Compile / fullLinkJS).value

          CLIHelper.bundleJs(
            CLIHelper.JSBundle(
              CLIHelper.Cmd(
                tool.build.command.command,
                tool.build.command.successPattern,
                tool.build.command.workingDir
              ),
              CLIHelper.Cmd(
                tool.installPackagesCommand.command,
                tool.installPackagesCommand.successPattern,
                tool.installPackagesCommand.workingDir
              ),
              tool.build.startupMessage,
              tool.build.successMessage
            )
          )
        case None =>
          sys.error("jsTool is not configured. Please define jsTool setting in your build.sbt")
      }
    },

    dev := {
      jsTool.?.value match {
        case Some(tool) =>
          val sourceDirs = (Compile / sourceDirectories).value.map(_.toPath).toList
          val taskKey = Compile / fastOptJS
          val sbtState = state.value

          val compilationRunner = () =>
            try
              Project.runTask(taskKey, sbtState) match {
                case Some((_, Value(_))) => true // Success
                case Some((_, Inc(_)))   => false // Compilation failed
                case None                =>
                  println(s"[Scala.js ERROR] fastOptJS task not found. Make sure you're running this command on a Scala.js project (e.g., 'sbt frontend/dev' in multi-project builds)")
                  false
              }
            catch {
              case e: Exception =>
                println(s"[Scala.js ERROR] Compilation error: ${e.getMessage}")
                false
            }

          val task = CLIHelper.FastOptJS(sourceDirs, compilationRunner)
          CLIHelper.startDevEnvironment(
            task,
            CLIHelper.JSDevServer(
              CLIHelper.Cmd(
                tool.dev.command.command,
                tool.dev.command.successPattern,
                tool.dev.command.workingDir
              ),
              CLIHelper.Cmd(
                tool.installPackagesCommand.command,
                tool.installPackagesCommand.successPattern,
                tool.installPackagesCommand.workingDir
              ),
              tool.dev.startupMessage,
              tool.dev.successMessage
            )
          )
        case None =>
          sys.error("jsTool is not configured. Please define jsTool setting in your build.sbt")
      }
    }
  )
}
