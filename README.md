# Hello World SBT Plugin

A simple sbt plugin demonstrating core plugin concepts in 2026.

## What This Plugin Demonstrates

1. **AutoPlugin**: Modern way to create sbt plugins that auto-enable
2. **Custom Tasks**: `hello` task that prints a greeting
3. **Settings**: `helloName` setting to customize the greeting
4. **Task Dependencies**: `helloMessage` task that uses settings
5. **Logging**: Using sbt's logging system
6. **Auto Import**: Making plugin keys available without imports

## Project Structure

```
sbt-hello-world-plugin/
├── build.sbt                          # Plugin project definition
├── src/main/scala/
│   └── com/example/hello/
│       └── HelloPlugin.scala          # Plugin implementation
└── test-project/                      # Test project to try the plugin
    ├── build.sbt
    └── project/
        ├── build.properties
        └── plugins.sbt                # Loads the plugin
```

## Key Concepts

### AutoPlugin
- `extends AutoPlugin` - Modern plugin base class
- `trigger = allRequirements` - Auto-enables for all projects
- Other triggers: `noTrigger`, `AllRequirements`, specific requirements

### autoImport Object
Keys defined here are automatically imported in build.sbt files:
- `taskKey[T]` - Defines a task that returns T
- `settingKey[T]` - Defines a setting of type T

### Settings Scopes
- `projectSettings` - Applied to each project
- `buildSettings` - Applied once per build
- `globalSettings` - Applied globally

## Testing the Plugin

```bash
cd test-project
sbt hello
```

## Publishing (Optional)

To use this plugin in other projects, publish it locally:

```bash
sbt publishLocal
```

Then in another project's `project/plugins.sbt`:
```scala
addSbtPlugin("com.example" % "sbt-hello-world-plugin" % "0.1.0")
```

## Customizing the Plugin

In your `build.sbt`:
```scala
helloName := "Your Name"  // Changes the greeting
```
