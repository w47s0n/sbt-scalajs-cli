# sbt-scalajs-cli

An sbt plugin that streamlines Scala.js development by integrating JavaScript tooling into your sbt workflow.

## Problem

When building Scala.js applications, you typically need to juggle multiple processes:
- Run `sbt ~fastOptJS` to watch and compile Scala code to JavaScript
- Separately run your dev server (Vite, webpack-dev-server, etc.)
- Manually coordinate compilation and bundling
- Switch between terminals and tools

This plugin unifies these workflows into simple sbt commands that handle both Scala.js compilation and JavaScript tooling automatically.

## Solution

- `sbt dev` - Start dev mode with file watching, auto-compilation, and dev server
- `sbt publishDist` - Build optimized Scala.js bundle and run production build

Configure once, then use familiar sbt commands for your entire full-stack workflow

## Installation

**1. Add the plugin to your project**

In `project/plugins.sbt`:
```scala
addSbtPlugin("com.w47s0n" % "sbt-scalajs-cli" % "0.1.1")
```

**2. Configure your JavaScript tooling**

In your Scala.js project's settings in `build.sbt`:
```scala
import com.w47s0n.scalajscli.ScalaJsCli.autoImport._

lazy val frontend = (project in file("frontend"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    jsTool := JSToolConfig(
      installPackagesCommand = Cmd("npm install", ".*".r),
      dev = DevConfig(
        command = Cmd("npm run dev", ".*ready.*".r),
        startupMessage = "Starting dev server...",
        successMessage = "Dev server ready!"
      ),
      build = BuildConfig(
        command = Cmd("npm run build", ".*built.*".r),
        startupMessage = "Building for production...",
        successMessage = "Build complete!"
      )
    )
  )
```

**3. Run your tasks**
```bash
sbt frontend/dev          # Start development mode
sbt frontend/publishDist  # Build for production
```

The plugin automatically enables on any project with ScalaJS enabled, but requires `jsTool` configuration to function.
