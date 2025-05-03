plugins {
    id("java")
    id("fabric-loom").version("1.10-SNAPSHOT")
    id("com.gradleup.shadow").version("9.0.0-beta12")
}

group = "io.github.lijinhong11"
version = "${project.properties["mod_version"]}"

base {
    archivesName = "${project.properties["archives_base_name"]}"
}

repositories {
    mavenCentral()
    maven("https://maven.shedaniel.me/")
    maven("https://maven.terraformersmc.com/releases/")
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    minecraft("com.mojang:minecraft:${properties["minecraft_version"]}")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:${properties["loader_version"]}")

    modImplementation("net.fabricmc.fabric-api:fabric-api:${properties["fabric_version"]}")

    implementation(project(":api"))

    //api
    modApi("me.shedaniel.cloth:cloth-config-fabric:11.1.136") {
        exclude("net.fabricmc.fabric-api")
    }

    modApi("com.terraformersmc:modmenu:7.2.2") {
        exclude("net.fabricmc.fabric-api")
    }
}

tasks.processResources {
    filesMatching("fabric.mod.json") {
        expand(project.properties)
    }
    exclude("mappings/mappings.tiny")
}

tasks.test {
    useJUnitPlatform()
}

tasks.shadowJar {
    archiveFileName.set("${project.properties["archives_base_name"]}-${project.version}-shadow-raw.jar")

    dependencies {
        include(project(":api"))
    }

    exclude("mappings/mappings.tiny")

    finalizedBy(tasks.remapJar)
}

tasks.remapJar {
    dependsOn(tasks.shadowJar)
    inputFile.set(tasks.shadowJar.flatMap { it.archiveFile })
    archiveFileName.set("${project.properties["archives_base_name"]}-${project.version}.jar")
}