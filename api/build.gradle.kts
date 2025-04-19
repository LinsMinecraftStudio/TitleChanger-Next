plugins {
    id("java")
    id("maven-publish")
}

group = "io.github.lijinhong11"
version = "1.0-SNAPSHOT"

base {
    archivesName = "titlechanger-api"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
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