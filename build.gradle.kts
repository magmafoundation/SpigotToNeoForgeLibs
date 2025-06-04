plugins {
    id("java")
    id("com.gradleup.shadow") version "9.0.0-beta15"
    id("maven-publish")
}

group = "org.magmafoundation"
version = System.getenv("RELEASE_VERSION") ?: "1.0.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.yaml:snakeyaml:2.2")
}

tasks.test {
    useJUnitPlatform()
}

tasks.shadowJar {
    archiveClassifier = null
    dependencies {
        include(dependency("org.yaml:snakeyaml:2.2"))
    }
    relocate("org.yaml.snakeyaml", "org.magmafoundation.magma.deps.snakeyaml")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifact(tasks.shadowJar)
            groupId = project.group.toString()
            artifactId = rootProject.name
            version = project.version.toString()
        }
    }
    repositories {
        maven {
            name = "magmaReleases"
            url = uri("https://repo.magmafoundation.org/releases")
            credentials {
                username = System.getenv("MAGMA_REPO_USER")
                password = System.getenv("MAGMA_REPO_PASS")
            }
        }
    }
}
