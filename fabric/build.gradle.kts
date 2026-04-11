plugins {
    java
    id("net.fabricmc.fabric-loom")
    id("com.gradleup.shadow")
}

group = "io.github.lijinhong11"
version = "${project.properties["mod_version"]}"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
    maven("https://maven.shedaniel.me/")
    maven("https://maven.terraformersmc.com/releases/")
}

sourceSets {
    main {
        resources.srcDir("../resources")
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${properties["minecraft_version"]}")

    implementation("net.fabricmc:fabric-loader:${properties["fabric_loader_version"]}")

    implementation("net.fabricmc.fabric-api:fabric-api:${properties["fabric_version"]}")

    implementation(project(":common"))
    implementation(project(":api"))

    //api
    api("me.shedaniel.cloth:cloth-config-fabric:${properties["cloth_config_version"]}") {
        exclude("net.fabricmc.fabric-api")
        exclude("net.fabricmc", "fabric-loader")
    }

    api("com.terraformersmc:modmenu:${properties["modmenu_version"]}") {
        exclude("net.fabricmc.fabric-api")
        exclude("net.fabricmc", "fabric-loader")
    }
}

tasks.processResources {
    filesMatching("fabric.mod.json") {
        expand(project.rootProject.rootProject.properties)
    }

    exclude("META-INF/neoforge.mods.toml")
    exclude("titlechanger-neoforge.mixins.json")
}

tasks.test {
    useJUnitPlatform()
}

tasks.shadowJar {
    archiveFileName.set("titlechanger-fabric-${project.version}.jar")

    dependencies {
        include(project(":api"))
        include(project(":common"))
    }
}