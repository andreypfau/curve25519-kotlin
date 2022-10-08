rootProject.name = "curve25519-kotlin"

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

    plugins {
        kotlin("multiplatform") version "1.7.20"
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
