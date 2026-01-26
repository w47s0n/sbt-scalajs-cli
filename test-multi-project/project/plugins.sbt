// Reference the plugin from parent directory for local testing
lazy val root = Project("plugins", file(".")).dependsOn(plugin)
lazy val plugin = RootProject(file("../").getCanonicalFile.toURI)
