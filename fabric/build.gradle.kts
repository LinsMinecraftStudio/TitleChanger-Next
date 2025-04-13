plugins {
    id("java")
    id("fabric-loom").version("1.10-SNAPSHOT")
}

group = "io.github.lijinhong11"
version = "1.0-SNAPSHOT"

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
    include(project(":api"))

    //api
    modApi("me.shedaniel.cloth:cloth-config-fabric:11.1.136") {
        exclude("net.fabricmc.fabric-api")
    }

    modApi("com.terraformersmc:modmenu:7.2.2") {
        exclude("net.fabricmc.fabric-api")
    }
}

tasks.test {
    useJUnitPlatform()
}