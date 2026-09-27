plugins {
    java
    id("fetch-version")
    id("net.fabricmc.fabric-loom") version "1.17-SNAPSHOT" apply false
    id("com.gradleup.shadow") version "9.6.1" apply false
}

group = "io.github.lijinhong11"
version = findProperty("mod_version")!! as String

dependencies {
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}