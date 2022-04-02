rootProject.name = "curve25519-kotlin"

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

    plugins {
        kotlin("multiplatform") version "1.6.10"
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
