plugins {
    java
    id("maven-publish")
}

group = "io.github.lijinhong11"
version = "1.0-SNAPSHOT"

base {
    archivesName = "titlechanger-api"
}


java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
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
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/LinsMinecraftStudio/TitleChanger-New")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN_PKG")
            }
        }
    }
}

 */