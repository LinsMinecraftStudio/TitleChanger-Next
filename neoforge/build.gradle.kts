import java.util.Properties

plugins {
    java
    id("net.neoforged.gradle.userdev") version "7.1.20"
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.3"
    id("com.gradleup.shadow") version "9.0.0"
}

val mod_version: String by project
val mod_group_id: String by project
val mod_id: String by project

project.version = mod_version
project.group = mod_group_id

repositories {
    mavenLocal()
    maven("https://maven.neoforged.net/releases")
    maven("https://maven.neoforged.net/snapshots")
    maven("https://maven.shedaniel.me/")
}

base {
    archivesName.set(mod_id)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

//minecraft.accessTransformers.file rootProject.file("src/main/resources/META-INF/accesstransformer.cfg")
//minecraft.accessTransformers.entry public net.minecraft.client.Minecraft textureManager # textureManager

runs {
    configureEach {
        systemProperty("forge.logging.markers", "REGISTRIES")

        systemProperty("forge.logging.console.level", "debug")

        modSource(project.sourceSets.getByName("main"))
    }

    create("client") {
        systemProperty("forge.enabledGameTestNamespaces", mod_id)

        dependencies {
            runtime(project(":api"))
            runtime(project(":"))
        }
    }

    create("server") {
        systemProperty("forge.enabledGameTestNamespaces", mod_id)
        argument("--nogui")
    }

    create("gameTestServer") {
        systemProperty("forge.enabledGameTestNamespaces", mod_id)
    }
}

sourceSets {
    main {
        resources.srcDirs("src/generated/resources/", "../resources")
    }
}

configurations {
    runtimeClasspath.get().extendsFrom(getByName("localRuntime"))
}

dependencies {
    val neo_version: String by project

    implementation("net.neoforged:neoforge:$neo_version")

    implementation(project(":api"))
    implementation(project(":"))

    compileOnly("me.shedaniel.cloth:cloth-config-neoforge:${properties["cloth_config_version"]}")
    localRuntime("me.shedaniel.cloth:cloth-config-neoforge:${properties["cloth_config_version"]}")

    // Example optional mod dependency with JEI
    // The JEI API is declared for compile time use, while the full JEI artifact is used at runtime
    // val jei_vesion: String by project
    // compileOnly("mezz.jei:jei-${minecraft_version}-common-api:${jei_version}")
    // compileOnly("mezz.jei:jei-${minecraft_version}-neoforge-api:${jei_version}")
    // We add the full version to localRuntime, not runtimeOnly, so that we do not publish a dependency on it
    // "localRuntime"("mezz.jei:jei-${minecraft_version}-neoforge:${jei_version}")

    // Example mod dependency using a mod jar from ./libs with a flat dir repository
    // This maps to ./libs/coolmod-${minecraft_version}-${coolmod_version}.jar
    // The group id is ignored when searching -- in this case, it is "blank"
    // val coolmod_version: String by project
    // implementation("blank:coolmod-${minecraft_version}:${coolmod_version}")

    // Example mod dependency using a file as dependency
    // implementation(files("libs/coolmod-${minecraft_version}-${coolmod_version}.jar"))
}

tasks.withType<ProcessResources>().configureEach {
    val loadedProperties = Properties().apply {
        load(project.rootProject.file("gradle.properties").inputStream())
    }.toMutableMap() as MutableMap<String, *>

    inputs.properties(loadedProperties)

    filesMatching("META-INF/neoforge.mods.toml") {
        expand(loadedProperties)
    }

    exclude("fabric.mod.json")
    exclude("titlechanger-fabric.mixins.json")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8" // Use the UTF-8 charset for Java compilation
}

tasks.shadowJar {
    dependsOn(project(":").tasks.shadowJar)

    archiveFileName.set("titlechanger-neoforge-1.21.X-${project.version}.jar")

    dependencies {
        exclude("fabric.mod.json")
        exclude("titlechanger-fabric.mixins.json")

        include(project(":api"))
        include(project(":"))
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}