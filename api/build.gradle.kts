plugins {
    java
    //id("maven-publish")
}

group = "io.github.lijinhong11"
version = "1.0"

base {
    archivesName = "titlechanger-api"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

dependencies {
}

/*
publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "io.github.lijinhong11"
            artifactId = "titlechanger-api"
            version = "1.0-SNAPSHOT"

            from(components["java"])
        }
    }

    repositories {
    }
}

 */