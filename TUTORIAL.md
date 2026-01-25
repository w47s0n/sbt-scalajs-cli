# SBT Plugin Tutorial

## How SBT Plugins Work (2026 Guide)

### 1. Plugin Basics

An sbt plugin is just a regular sbt project with `sbtPlugin := true` in its build.sbt.

**Key Files:**
- `build.sbt` - Defines the plugin project itself
- `src/main/scala/**/*.scala` - Plugin source code
- `project/build.properties` - Specifies sbt version

### 2. AutoPlugin Pattern

Modern plugins extend `AutoPlugin`:

```scala
object MyPlugin extends AutoPlugin {
  override def trigger = allRequirements  // When to enable
  override def requires = empty           // Plugin dependencies

  object autoImport { /* Keys visible to users */ }
  override lazy val projectSettings = Seq(/* ... */)
}
```

**Trigger Options:**
- `allRequirements` - Auto-enable for all projects
- `noTrigger` - Must be manually enabled with `enablePlugins(MyPlugin)`
- Custom requirements

### 3. Settings vs Tasks

**Settings** (`settingKey[T]`):
- Evaluated once at project load
- Use `.value` to reference other settings
- Example: `name`, `version`, `scalaVersion`

**Tasks** (`taskKey[T]`):
- Evaluated each time they run
- Can have side effects
- Example: `compile`, `test`, `package`

**Input Tasks** (`inputKey[T]`):
- Tasks that accept command-line arguments
- Example: `run`, `testOnly`

### 4. The autoImport Object

Keys defined here are automatically available in build.sbt files:

```scala
object autoImport {
  val myTask = taskKey[Unit]("Does something")
  val mySetting = settingKey[String]("Configures something")
}
```

### 5. Task Implementation

```scala
myTask := {
  val log = streams.value.log           // Get logger
  val projectName = name.value          // Get settings
  val srcDirs = (Compile / sources).value  // Get task results

  log.info("Doing something...")
  // Task logic here
}
```

### 6. Task Dependencies

Tasks automatically depend on values they access via `.value`:

```scala
myTask := {
  val compiled = (Compile / compile).value  // Ensures compilation first
  val resources = (Compile / resources).value
  // Now we can use compiled classes and resources
}
```

### 7. Configuration Scopes

```scala
// Applies to Compile configuration
Compile / sourceDirectories += file("extra-sources")

// Applies to Test configuration
Test / parallelExecution := false

// Applies to all configurations
sources := Seq.empty
```

### 8. Common Plugin Patterns

**Pattern 1: Adding a Simple Task**
```scala
val hello = taskKey[Unit]("Says hello")
hello := {
  println("Hello!")
}
```

**Pattern 2: Task Using Settings**
```scala
val printVersion = taskKey[Unit]("Prints version")
printVersion := {
  println(s"Version: ${version.value}")
}
```

**Pattern 3: Task Using Other Tasks**
```scala
val compileAndPrint = taskKey[Unit]("Compiles and prints")
compileAndPrint := {
  (Compile / compile).value  // Run compilation
  println("Compiled successfully!")
}
```

**Pattern 4: Modifying Existing Settings**
```scala
// Add to existing sequence
sourceDirectories += file("extra")

// Replace completely
sourceDirectories := Seq(file("only-this"))

// Transform existing value
libraryDependencies ++= Seq(/* new deps */)
```

**Pattern 5: Conditional Settings**
```scala
libraryDependencies ++= {
  if (scalaVersion.value.startsWith("2.13"))
    Seq("org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.4")
  else
    Seq.empty
}
```

### 9. Testing Your Plugin

**Option 1: Local Test Project** (like this example)
```
my-plugin/
├── build.sbt                 # Plugin definition
├── src/main/scala/           # Plugin code
└── test-project/             # Test project
    ├── build.sbt
    └── project/plugins.sbt   # Loads parent plugin
```

**Option 2: Scripted Tests**
```scala
// In plugin's build.sbt
scriptedLaunchOpts := { scriptedLaunchOpts.value ++
  Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
}
scriptedBufferLog := false

// Create tests in src/sbt-test/my-plugin/test-name/
```

**Option 3: Publish Locally**
```bash
sbt publishLocal

# Then in another project's project/plugins.sbt:
# addSbtPlugin("com.example" % "my-plugin" % "0.1.0")
```

### 10. Plugin Commands

Plugins can also add custom commands:

```scala
val myCommand = Command.command("myCommand") { state =>
  println("Running custom command")
  state
}

override def globalSettings = Seq(
  commands += myCommand
)
```

### 11. Best Practices

1. **Use AutoPlugin** - Don't extend `Plugin` (deprecated)
2. **Use autoImport** - Makes keys available without imports
3. **Provide defaults** - Settings should have sensible defaults
4. **Use scopes properly** - Don't pollute global scope unnecessarily
5. **Document keys** - Use descriptive task/setting descriptions
6. **Test thoroughly** - Use scripted tests for complex plugins
7. **Version carefully** - Follow semantic versioning

### 12. Publishing Your Plugin

```bash
# Publish to local ivy repository
sbt publishLocal

# Publish to Maven Central (requires setup)
sbt publishSigned
sbt sonatypeRelease
```

### 13. Common Use Cases

- **Code generation** - Generate Scala/Java code before compilation
- **Resource processing** - Transform or validate resources
- **Custom testing** - Add specialized test frameworks
- **Build validation** - Check dependencies, licenses, etc.
- **Deployment** - Package and deploy artifacts
- **IDE integration** - Generate IDE project files

## Examples in This Project

- **HelloPlugin.scala** - Basic plugin with tasks and settings
- **AdvancedPlugin.scala** - More complex patterns (requires, file generation)

## Try It Out

```bash
# From the test-project directory:
cd test-project

# Run the hello task
sbt hello

# Customize the greeting
sbt 'set helloName := "Your Name"' hello

# See all tasks from the plugin
sbt tasks -V | grep hello

# Enable advanced plugin in build.sbt:
# enablePlugins(AdvancedPlugin)
```
