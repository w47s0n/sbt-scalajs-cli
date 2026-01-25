name := "sbt-scalajs-cli"
organization := "com.w47s0n"
version := "0.1.0"

sbtPlugin := true

scalaVersion := "2.12.21"

// Project metadata
description := "An sbt plugin for Scala.js development with built-in dev server support"
licenses := Seq(
  "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")
)
homepage := Some(url("https://github.com/w47s0n/sbt-scalajs-cli"))
scmInfo := Some(
  ScmInfo(
    url("https://github.com/w47s0n/sbt-scalajs-cli"),
    "scm:git@github.com:w47s0n/sbt-scalajs-cli.git"
  )
)
developers := List(
  Developer(
    "w47s0n",
    "Watson Dinh",
    "ping@w47s0n.com",
    url("https://github.com/w47s0n")
  )
)

// Publishing settings
publishMavenStyle := true
publishTo := sonatypePublishToBundle.value
sonatypeCredentialHost := "central.sonatype.com"
sonatypeRepository := "https://central.sonatype.com/api/v1/publisher"

// Plugin dependencies
addSbtPlugin(
  "org.scala-js" % "sbt-scalajs" % "1.20.2"
) // Q: What happens if client (plugin users) has different scalajs version ?

libraryDependencies += "com.w47s0n" %% "consolebox" % "0.2.1"
