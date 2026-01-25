package com.example.hello

import sbt._
import sbt.Keys._
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * A more advanced plugin example showing additional sbt plugin patterns
 */
object AdvancedPlugin extends AutoPlugin {

  // Only enable if ScalaJsCli is enabled
  override def requires = ScalaJsCli

  // Don't auto-enable, must be explicitly enabled
  override def trigger = noTrigger

  object autoImport {
    val greetWithTime = taskKey[Unit]("Greets with current time")
    val generateGreetingFile = taskKey[File]("Generates a greeting file")
    val greetingPrefix = settingKey[String]("Prefix for greetings")
  }

  import autoImport._

  override lazy val projectSettings = Seq(
    greetingPrefix := "Greetings",

    greetWithTime := {
      val log = streams.value.log
      val prefix = greetingPrefix.value
      val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
      val now = LocalDateTime.now().format(formatter)

      log.info(s"$prefix at $now")
      log.info(s"This is the ${name.value} project")
    },

    generateGreetingFile := {
      val log = streams.value.log
      val targetDir = (Compile / target).value
      val greetingFile = targetDir / "greeting.txt"

      val content = s"""
        |Project: ${name.value}
        |Version: ${version.value}
        |Generated: ${LocalDateTime.now()}
        |Greeting: ${ScalaJsCli.autoImport.helloMessage.value}
      """.stripMargin

      IO.write(greetingFile, content)
      log.info(s"Generated greeting file: ${greetingFile.absolutePath}")

      greetingFile
    },

    // Add the generated file to resources
    Compile / resources ++= Seq(generateGreetingFile.value)
  )
}
