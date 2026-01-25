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

  case class Cmd(command: String, successPattern: Regex)

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
      val _ = (Compile / fullLinkJS).value
      val tool = jsTool.value

      CLIHelper.bundleJs(
        CLIHelper.JSBundle(
          CLIHelper.Cmd(
            tool.build.command.command,
            tool.build.command.successPattern
          ),
          CLIHelper.Cmd(
            tool.installPackagesCommand.command,
            tool.installPackagesCommand.successPattern
          ),
          tool.build.startupMessage,
          tool.build.successMessage
        )
      )
    },

    dev := {
      val sourceDirs = (Compile / sourceDirectories).value.map(_.toPath).toList
      val taskKey = Compile / fastOptJS
      val sbtState = state.value
      val tool = jsTool.value

      val compilationRunner = () =>
        try
          Project.runTask(taskKey, sbtState) match {
            case Some((_, Value(_))) => true // Success
            case Some((_, Inc(_)))   => false // Compilation failed
            case None                =>
              println(s"[Scala.js ERROR] Task $taskKey not found")
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
            tool.dev.command.successPattern
          ),
          CLIHelper.Cmd(
            tool.installPackagesCommand.command,
            tool.installPackagesCommand.successPattern
          ),
          tool.dev.startupMessage,
          tool.dev.successMessage
        )
      )
    }
  )
}
