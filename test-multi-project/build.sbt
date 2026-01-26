import com.w47s0n.scalajscli.ScalaJsCli.autoImport._

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

    // Configure jsTool for this ScalaJS project
    jsTool := JSToolConfig(
      installPackagesCommand =
        Cmd("npm install --prefix frontend", "d+ package".r),
      dev = DevConfig(
        command = Cmd("npm run --prefix frontend dev", ".*ready.*".r),
        startupMessage = "Starting Vite dev server...",
        successMessage = "Vite dev server ready!"
      ),
      build = BuildConfig(
        command = Cmd("npm run build", ".*built.*".r),
        startupMessage = "Building with Vite...",
        successMessage = "Vite build complete!"
      )
    )
  )
