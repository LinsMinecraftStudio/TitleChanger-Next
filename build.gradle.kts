plugins {
    java
    `java-library`
    id("fabric-loom").version("1.10-SNAPSHOT")
    id("com.gradleup.shadow").version("9.0.0-beta13")
}

group = "io.github.lijinhong11"
version = "1.0-SNAPSHOT"

dependencies {
    minecraft("com.mojang:minecraft:${properties["minecraft_version"]}")
    mappings(loom.officialMojangMappings())

    implementation(project(":api"))
}

tasks.shadowJar {
    archiveFileName.set("titlechanger-common.jar")

    dependencies {
        include(project(":api"))
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}