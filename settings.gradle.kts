pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases")
        gradlePluginPortal()
    }
}

rootProject.name = "titlechanger-container"

include("api")
include("common")
include("fabric")
include("neoforge")

include("neoforge")