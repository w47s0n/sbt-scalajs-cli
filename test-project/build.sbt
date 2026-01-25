name := "test-project"
version := "0.1.0"
scalaVersion := "2.13.18"

enablePlugins(ScalaJSPlugin)

// Scala.js configuration
scalaJSUseMainModuleInitializer := true

// Add scala-js-dom dependency
libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.4.0"

// Configure the dev server
jsDevServer := JSDevServer(
  Cmd("npm run dev", "VITE.*ready".r),
  Cmd("npm install", ".*added.*packages".r),
  "Starting Vite development environment",
  "Development server is ready!"
)
