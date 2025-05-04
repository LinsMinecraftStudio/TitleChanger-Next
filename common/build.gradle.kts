plugins {
    id("java")
    id("com.gradleup.shadow").version("9.0.0-beta12")
}

group = "io.github.lijinhong11.titlechanger"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":api"))
    implementation("com.google.code.gson:gson:2.13.0")
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