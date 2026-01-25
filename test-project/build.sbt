name := "test-project"
version := "0.1.0"
scalaVersion := "2.13.18"

enablePlugins(ScalaJSPlugin)

// Scala.js configuration
scalaJSUseMainModuleInitializer := true

// Add scala-js-dom dependency
libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.4.0"

// Configure JavaScript tooling (npm, bun, yarn, etc.)
jsTool := JSToolConfig(
  installPackagesCommand = Cmd("npm install", ".*added.*packages".r),
  dev = DevConfig(
    command = Cmd("npm run dev", "VITE.*ready".r),
    startupMessage = "Starting Vite development environment",
    successMessage = "Development server is ready!"
  ),
  build = BuildConfig(
    command = Cmd("npm run build", "built in".r),
    startupMessage = "Building production JavaScript bundle",
    successMessage = "Production build completed successfully!"
  )
)
