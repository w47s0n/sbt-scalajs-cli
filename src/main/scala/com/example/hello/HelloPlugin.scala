package com.example.hello

import sbt._
import sbt.Keys._

object HelloPlugin extends AutoPlugin {

  // This plugin will automatically be enabled for all projects
  override def trigger = allRequirements

  // Define custom settings and tasks
  object autoImport {
    val hello = taskKey[Unit]("Prints Hello World")
    val helloName = settingKey[String]("Name to greet")
    val helloMessage = taskKey[String]("Generates a greeting message")
  }

  import autoImport._

  // Default settings that will be automatically added to projects
  override lazy val projectSettings = Seq(
    helloName := "World",

    helloMessage := {
      val name = helloName.value
      s"Hello, $name! This is an sbt plugin."
    },

    hello := {
      val log = streams.value.log
      val message = helloMessage.value

      log.info("=" * 50)
      log.info(message)
      log.info(s"Project: ${name.value}")
      log.info(s"Version: ${version.value}")
      log.info(s"Scala Version: ${scalaVersion.value}")
      log.info("=" * 50)
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
