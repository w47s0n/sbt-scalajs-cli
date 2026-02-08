import com.w47s0n.scalajscli.ScalaJsCli.autoImport._

// Define the task key
lazy val generateScalaJSConfig = taskKey[Unit]("Generate scalajs-config.json with paths")

// Root project (no ScalaJS)
lazy val root = (project in file("."))
  .aggregate(core, frontend)
  .settings(
    name := "test-multi-project",
    version := "0.1.0",
    scalaVersion := "3.8.1"
  )

// Regular Scala project (no ScalaJS)
lazy val core = (project in file("core"))
  .settings(
    name := "core",
    scalaVersion := "3.8.1"
  )

// ScalaJS project
lazy val frontend = (project in file("frontend"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := "frontend",
    scalaVersion := "3.8.1",
    scalaJSUseMainModuleInitializer := true,

    // Generate config file with Scala version for Vite
    generateScalaJSConfig := {
      val scalaVer = scalaVersion.value
      val projectName = name.value
      val config = s"""{
  "scalaVersion": "$scalaVer",
  "projectName": "$projectName",
  "fastOptPath": "/target/scala-$scalaVer/$projectName-fastopt/main.js",
  "fullOptPath": "/target/scala-$scalaVer/$projectName-opt/main.js"
}"""
      val configFile = baseDirectory.value / "scalajs-config.json"
      IO.write(configFile, config)
      streams.value.log.info(s"Generated scalajs-config.json")
    },

    // Run config generation before compilation
    Compile / fastOptJS := ((Compile / fastOptJS) dependsOn generateScalaJSConfig).value,
    Compile / fullOptJS := ((Compile / fullOptJS) dependsOn generateScalaJSConfig).value,

// Source maps seem to be broken with bundler
    Compile / fastOptJS / scalaJSLinkerConfig ~= { _.withSourceMap(false) },
    Compile / fullOptJS / scalaJSLinkerConfig ~= { _.withSourceMap(false) },
    // Configure jsTool for this ScalaJS project
    jsTool := JSToolConfig(
      installPackagesCommand = Cmd.npm.install.withPrefix("frontend"),
      dev = DevConfig(
        command = Cmd.npm.run("dev").withPrefix("frontend"),
        startupMessage = "Starting Vite dev server...",
        successMessage = "Vite dev server ready!"
      ),
      build = BuildConfig(
        command = Cmd.npm.build.withPrefix("frontend"),
        startupMessage = "Building with Vite...",
        successMessage = "Vite build complete!"
      )
    )
  )
