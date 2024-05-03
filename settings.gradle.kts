rootProject.name = "curve25519-kotlin"

pluginManagement {
    includeBuild("build-logic")

    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

    plugins {
        kotlin("multiplatform") version "2.0.0-RC2"
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
