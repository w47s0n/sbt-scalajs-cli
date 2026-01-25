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
    val hello = taskKey[Unit]("Prints Hello World")
    val dev = taskKey[Unit]("Start dev mode with continuous Scala.js compilation")
  }

  import autoImport._

  // Default settings that will be automatically added to projects
  override lazy val projectSettings = Seq(
    dev := {
      val sourceDirs = (Compile / sourceDirectories).value
      val scalaVer   = scalaVersion.value
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

      CLIHelper.startDevEnvironment(
        scalaVer,
        sourceDirs,
        compilationRunner,
        Seq("npm", "run", "dev")
      )
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
