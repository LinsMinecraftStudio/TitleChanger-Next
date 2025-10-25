plugins {
    java
    id("fabric-loom").version("1.10-SNAPSHOT")
    id("com.gradleup.shadow").version("9.0.0")
}

group = "io.github.lijinhong11"
version = "${project.rootProject.properties["mod_version"]}"

base {
    archivesName = "titlechanger-fabric"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
    maven("https://maven.shedaniel.me/")
    maven("https://maven.terraformersmc.com/releases/")
    maven("https://maven.parchmentmc.org")
}

sourceSets {
    main {
        resources.srcDir("../resources")
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${properties["minecraft_version"]}")
    mappings(loom.layered {
        officialMojangMappings()

        parchment("org.parchmentmc.data:parchment-${properties["minecraft_version"]}:${properties["neogradle.subsystems.parchment.mappingsVersion"]}@zip")
    })
    modImplementation("net.fabricmc:fabric-loader:${properties["fabric_loader_version"]}")

    modImplementation("net.fabricmc.fabric-api:fabric-api:${properties["fabric_version"]}")

    implementation(project(":"))
    implementation(project(":api"))

    //api
    modApi("me.shedaniel.cloth:cloth-config-fabric:${properties["cloth_config_version"]}") {
        exclude("net.fabricmc.fabric-api")
        exclude("net.fabricmc", "fabric-loader")
    }

    modApi("com.terraformersmc:modmenu:${properties["modmenu_version"]}") {
        exclude("net.fabricmc.fabric-api")
        exclude("net.fabricmc", "fabric-loader")
    }
}

tasks.processResources {
    filesMatching("fabric.mod.json") {
        expand(mapOf("version" to project.version, "cloth_config_version" to properties["cloth_config_version"]))
    }

    exclude("mappings/mappings.tiny")
    exclude("META-INF/neoforge.mods.toml")
    exclude("titlechanger-neoforge.mixins.json")
}

tasks.test {
    useJUnitPlatform()
}

tasks.shadowJar {
    dependsOn(project(":").tasks.shadowJar)

    archiveFileName.set("titlechanger-fabric-1.21.X-${project.version}-shadow-raw.jar")

    dependencies {
        include(project(":api"))
        include(project(":"))

        exclude("mappings/mappings.tiny")
    }

    finalizedBy(tasks.remapJar)
}

tasks.remapJar {
    dependsOn(tasks.shadowJar)
    inputFile.set(tasks.shadowJar.flatMap { it.archiveFile })
    archiveFileName.set("titlechanger-fabric-1.21.X-${project.version}.jar")
}