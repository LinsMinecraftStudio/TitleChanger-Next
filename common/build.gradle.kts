plugins {
    java
    id("net.fabricmc.fabric-loom")
}

group = "io.github.lijinhong11"
version = properties["mod_version"]!!

dependencies {
    minecraft("com.mojang:minecraft:${properties["minecraft_version"]}")

    implementation(project(":api"))
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}