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

  case class JSDevServer(
      runCommand: Cmd,
      installPackagesCommand: Cmd,
      startupMessage: String,
      successMessage: String
  )

  // Define custom settings and tasks
  object autoImport {
    val dev =
      taskKey[Unit]("Start dev mode with continuous Scala.js compilation")
    val jsDevServer = settingKey[JSDevServer](
      "JavaScript dev server configuration (command, patterns, messages)"
    )

    // Export case classes for user convenience
    type Cmd = ScalaJsCli.Cmd
    val Cmd = ScalaJsCli.Cmd
    type JSDevServer = ScalaJsCli.JSDevServer
    val JSDevServer = ScalaJsCli.JSDevServer
  }

  import autoImport._

  // Default settings that will be automatically added to projects
  override lazy val projectSettings = Seq(
    dev := {
      val sourceDirs = (Compile / sourceDirectories).value.map(_.toPath).toList
      val taskKey = Compile / fastOptJS
      val sbtState = state.value
      val devServer = jsDevServer.value

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
            devServer.runCommand.command,
            devServer.runCommand.successPattern
          ),
          CLIHelper.Cmd(
            devServer.installPackagesCommand.command,
            devServer.installPackagesCommand.successPattern
          ),
          devServer.startupMessage,
          devServer.successMessage
        )
      )
    }
  )

  // Global settings (applied once per build)
  override lazy val globalSettings = Seq(
    // Add any global settings here
  )

  // Build-level settings
  override lazy val buildSettings = Seq(
    // Add any build-level settings here
  )
}
