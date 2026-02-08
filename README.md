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
addSbtPlugin("com.w47s0n" % "sbt-scalajs-cli" % "0.2.0")
```

**2. Configure your JavaScript tooling**

In your Scala.js project's settings in `build.sbt`:
```scala
import com.w47s0n.scalajscli.ScalaJsCli.autoImport._

lazy val frontend = (project in file("frontend"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    jsTool := JSToolConfig(
      installPackagesCommand = Cmd.npm.install,
      dev = DevConfig(
        command = Cmd.npm.run("dev"),
        startupMessage = "Starting dev server...",
        successMessage = "Dev server ready!"
      ),
      build = BuildConfig(
        command = Cmd.npm.build,
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

## Package Manager Support

The plugin provides type-safe builders for popular package managers:

### npm (default)
```scala
jsTool := JSToolConfig(
  installPackagesCommand = Cmd.npm.install,
  dev = DevConfig(
    command = Cmd.npm.run("dev"),
    startupMessage = "Starting Vite dev server...",
    successMessage = "Dev server ready!"
  ),
  build = BuildConfig(
    command = Cmd.npm.build,
    startupMessage = "Building for production...",
    successMessage = "Build complete!"
  )
)
```

### Yarn
```scala
jsTool := JSToolConfig(
  installPackagesCommand = Cmd.yarn.install,
  dev = DevConfig(
    command = Cmd.yarn.run("dev"),
    // ...
  ),
  // ...
)
```

### Bun
```scala
jsTool := JSToolConfig(
  installPackagesCommand = Cmd.bun.install,
  dev = DevConfig(
    command = Cmd.bun.dev,  // or Cmd.bun.run("dev")
    // ...
  ),
  // ...
)
```

### pnpm
```scala
jsTool := JSToolConfig(
  installPackagesCommand = Cmd.pnpm.install,
  dev = DevConfig(
    command = Cmd.pnpm.run("dev"),
    // ...
  ),
  // ...
)
```

## Multi-Project Setup

For multi-project builds where your frontend code is in a subdirectory, use `.withPrefix()` to set the working directory:

```scala
// Directory structure:
// .
// ├── build.sbt
// ├── core/           (regular Scala)
// └── frontend/       (Scala.js + npm)
//     ├── package.json
//     └── src/

lazy val frontend = (project in file("frontend"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
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
```

Then run:
```bash
sbt frontend/dev  # Runs npm from the frontend/ directory
```

## API Reference

### Available Commands

Each package manager builder provides these methods:

- `.install` - Install dependencies
- `.run(script)` - Run a package.json script
- `.dev` - Shorthand for `.run("dev")`
- `.build` - Shorthand for `.run("build")`
- `.add(packages*)` - Add packages (uses `yarn add` for Yarn, `npm install` for others)
- `.custom(subcommand, args*)` - Build custom commands

### Chainable Modifiers

Commands can be customized with these fluent methods:

- `.withPrefix(path)` - Set working directory (for multi-project builds)
- `.withWorkingDir(dir)` - Set working directory using a File object
- `.matching(regex)` - Override success pattern detection
- `.withArgs(args*)` - Add additional command arguments

### Examples

```scala
// Custom success pattern
Cmd.npm.run("dev").matching("Server started on.*".r)

// Additional arguments
Cmd.npm.install.withArgs("--legacy-peer-deps")

// Combined
Cmd.bun.run("preview")
  .withPrefix("frontend")
  .matching(".*ready.*".r)
```
