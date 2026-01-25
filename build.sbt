name := "sbt-hello-world-plugin"
organization := "com.example"
version := "0.1.0"

sbtPlugin := true

scalaVersion := "2.12.21"

// Plugin dependencies
addSbtPlugin(
  "org.scala-js" % "sbt-scalajs" % "1.20.2"
) // Q: What happens if client (plugin users) has different scalajs version ?

libraryDependencies += "com.w47s0n" %% "consolebox" % "0.2.1"
