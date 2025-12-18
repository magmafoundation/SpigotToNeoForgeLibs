plugins {
    id("java")
    id("com.gradleup.shadow") version "9.0.0-beta15"
    id("maven-publish")
}

group = "org.magmafoundation"
version = (project.findProperty("releaseVersion") as String?) ?: System.getenv("RELEASE_VERSION") ?: "1.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

fun org.gradle.api.tasks.bundling.Jar.deletePackage(packageName: String) {
    val path = packageName.replace('.', '/')
    exclude("$path/**")
}

dependencies {
    implementation("org.yaml:snakeyaml:2.2")
    implementation("commons-lang:commons-lang:2.6")
}

tasks.test {
    useJUnitPlatform()
}

tasks.shadowJar {
    archiveClassifier = null
    dependencies {
        include(dependency("org.yaml:snakeyaml:2.2"))
        include(dependency("commons-lang:commons-lang:2.6"))
    }
    relocate("org.yaml.snakeyaml", "org.magmafoundation.magma.deps.snakeyaml")
    relocate("org.apache.commons.lang", "org.magmafoundation.magma.deps.commonslang")

    deletePackage("org.apache.commons.lang.enum")
    deletePackage("org.apache.commons.lang.enums")
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
