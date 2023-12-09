buildscript {
    dependencies {
        classpath("com.android.tools.build:gradle:8.2.0")
    }
}
plugins {
    id("com.android.application").version("8.2.0") apply false
    id("com.android.library").version("8.2.0") apply false
    id("org.jetbrains.kotlin.android").version("1.9.21") apply false
    id("org.jetbrains.kotlin.jvm").version("1.9.21") apply false
    id("org.sonarqube").version("4.4.1.3373")
    id("org.jlleitschuh.gradle.ktlint") version "11.6.0"
}

sonarqube {
    properties {
        property("sonar.projectKey", "org.piepmeyer.gauguin")
        property("sonar.organization", "meikpiep")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}

tasks.sonar {
    onlyIf("There is no property 'buildserver'") {
        project.hasProperty("buildserver")
    }
    dependsOn(":gauguin-app:lint")
}

allprojects {
    repositories {
        mavenCentral()
        google()
    }
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
}
