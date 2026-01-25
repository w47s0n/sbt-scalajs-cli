// Load the plugin from the parent directory
lazy val helloPlugin = RootProject(file("..").getAbsoluteFile.toURI)
dependsOn(helloPlugin)
