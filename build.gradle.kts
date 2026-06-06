plugins {
    java
    id("fetch-version")
    id("net.fabricmc.fabric-loom") version "1.16-SNAPSHOT" apply false
    id("com.gradleup.shadow") version "9.4.1" apply false
}

group = "io.github.lijinhong11"
version = properties["mod_version"]!!

dependencies {
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}