name := "test-project"
version := "0.1.0"
scalaVersion := "2.13.18"

enablePlugins(ScalaJSPlugin)

// Scala.js configuration
scalaJSUseMainModuleInitializer := true

// Generate config file with Scala version for Vite
lazy val generateScalaJSConfig = taskKey[Unit]("Generate scalajs-config.json with paths")
generateScalaJSConfig := {
  val binaryVersion = scalaBinaryVersion.value
  val projectName = name.value
  val config = s"""{
  "scalaBinaryVersion": "$binaryVersion",
  "projectName": "$projectName",
  "fastOptPath": "/target/scala-$binaryVersion/$projectName-fastopt/main.js",
  "fullOptPath": "/target/scala-$binaryVersion/$projectName-opt/main.js"
}"""
  val configFile = baseDirectory.value / "scalajs-config.json"
  IO.write(configFile, config)
  streams.value.log.info(s"Generated scalajs-config.json")
}

// Run config generation before compilation
Compile / fastOptJS := ((Compile / fastOptJS) dependsOn generateScalaJSConfig).value
Compile / fullOptJS := ((Compile / fullOptJS) dependsOn generateScalaJSConfig).value

// Source maps seem to be broken with bundler
Compile / fastOptJS / scalaJSLinkerConfig ~= { _.withSourceMap(false) }
Compile / fullOptJS / scalaJSLinkerConfig ~= { _.withSourceMap(false) }
// Add scala-js-dom dependency
libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.4.0"

// Configure JavaScript tooling (npm, bun, yarn, etc.)
jsTool := JSToolConfig(
  installPackagesCommand = Cmd.npm.install,
  dev = DevConfig(
    command = Cmd.npm.run("dev"),
    startupMessage = "Starting Vite development environment",
    successMessage = "Development server is ready!"
  ),
  build = BuildConfig(
    command = Cmd.npm.build,
    startupMessage = "Building production JavaScript bundle",
    successMessage = "Production build completed successfully!"
  )
)
