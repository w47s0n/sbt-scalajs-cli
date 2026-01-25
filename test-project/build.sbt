name := "test-project"
version := "0.1.0"
scalaVersion := "2.13.18"

enablePlugins(ScalaJSPlugin)

// Scala.js configuration
scalaJSUseMainModuleInitializer := true

// Add scala-js-dom dependency
libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.4.0"
