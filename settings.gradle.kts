pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        gradlePluginPortal()
    }
}

rootProject.name = "titlechanger-container"

include("api")
include("fabric")
