package com.w47s0n.scalajscli

import sbt._
import sbt.Keys._
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import com.w47s0n.scalajscli.util.CLIHelper

object ScalaJsCli extends AutoPlugin {

  // This plugin will automatically be enabled when ScalaJS plugin is enabled
  override def requires = ScalaJSPlugin
  override def trigger = allRequirements

  // Define custom settings and tasks
  object autoImport {
    val dev = taskKey[Unit]("Start dev mode with continuous Scala.js compilation")
  }

  import autoImport._

  // Default settings that will be automatically added to projects
  override lazy val projectSettings = Seq(
    dev := {
      val sourceDirs = (Compile / sourceDirectories).value.map(_.toPath).toList
      val taskKey    = Compile / fastOptJS
      val sbtState   = state.value

      val compilationRunner = () =>
        try
          Project.runTask(taskKey, sbtState) match {
            case Some((_, Value(_))) => true  // Success
            case Some((_, Inc(_)))   => false // Compilation failed
            case None =>
              println(s"[Scala.js ERROR] Task $taskKey not found")
              false
          }
        catch {
          case e: Exception =>
            println(s"[Scala.js ERROR] Compilation error: ${e.getMessage}")
            false
        }

      val task = CLIHelper.FastOptJS(sourceDirs, compilationRunner)
      val jsDevServer = CLIHelper.JSDevServer(
        CLIHelper.Cmd("npm run dev", "VITE.*ready".r), // VITE v5.4.21  ready in 157 ms
        CLIHelper.Cmd("npm install", "packages installed".r),
        "Starting development environment",
        "Happy coding"
        )
      CLIHelper.startDevEnvironment(task, jsDevServer)
    },
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
