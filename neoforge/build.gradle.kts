import java.util.Properties

plugins {
    `java-library`
    java
    idea
    id("net.neoforged.gradle.userdev") version "7.0.184"
    id("com.gradleup.shadow") version "9.0.0-beta13"
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

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

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

    create("data") {
        // example of overriding the workingDirectory set in configureEach above, uncomment if you want to use it
        // workingDirectory project.file("run-data")

        // Specify the modid for data generation, where to output the resulting resource, and where to look for existing resources.
        arguments.addAll(listOf(
            "--mod", mod_id,
            "--all", "--output", file("src/generated/resources/").absolutePath,
            "--existing", file("../resources/").absolutePath))
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

    api("me.shedaniel.cloth:cloth-config-neoforge:13.0.138")

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

// This block of code expands all declared replace properties in the specified resource targets.
// A missing property will result in an error. Properties are expanded using ${} Groovy notation.
// When "copyIdeResources" is enabled, this will also run before the game launches in IDE environments.
// See https://docs.gradle.org/current/dsl/org.gradle.language.jvm.tasks.ProcessResources.html
tasks.withType<ProcessResources>().configureEach {
    val loadedProperties = Properties().apply {
        load(project.rootProject.file("gradle.properties").inputStream())
    }.toMutableMap() as MutableMap<String, Any>

    inputs.properties(loadedProperties)

    filesMatching("META-INF/mods.toml") {
        expand(loadedProperties)
    }

    exclude("fabric.mod.json")
    exclude("titlechanger-fabric.mixins.json")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8" // Use the UTF-8 charset for Java compilation
}

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}

tasks.shadowJar {
    dependsOn(project(":").tasks.shadowJar)

    archiveFileName.set("titlechanger-neoforge-${project.version}.jar")

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