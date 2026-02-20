name := "sbt-scalajs-cli"
organization := "com.w47s0n"

sbtPlugin := true

ThisBuild / versionScheme := Some("semver-spec")
ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / publishMavenStyle := true
sonatypeCredentialHost := "central.sonatype.com"

scalaVersion := "2.12.21"

description := "An sbt plugin for working with bundle tools"
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

// Plugin dependencies
addSbtPlugin(
  "org.scala-js" % "sbt-scalajs" % "1.20.2"
)
// Q: What happens if client (plugin users) has different scalajs version ?
// A: The safest approach is using % "provided" and documenting the compatible version range.

libraryDependencies += "com.w47s0n" %% "consolebox" % "0.2.1"
