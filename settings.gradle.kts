pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        gradlePluginPortal()
    }
}

rootProject.name = "titlechanger"
include("api")
include("fabric")
include("titlechanger-fabric")
