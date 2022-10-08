import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")
    `maven-publish`
}

allprojects {
    group = "com.github.andreypfau"
    version = "1.0-SNAPSHOT"

    apply(plugin = "kotlin-multiplatform")
    apply(plugin = "maven-publish")

    repositories {
        mavenLocal()
        mavenCentral()
    }

    kotlin {
        jvm {
            withJava()
            compilations.all {
                kotlinOptions.jvmTarget = "1.8"
            }
            testRuns["test"].executionTask.configure {
                useJUnitPlatform()
            }
        }
        macosArm64()
        macosX64()
        linuxX64()
        mingwX64()
        sourceSets {
            val commonMain by getting {
                dependencies {
                    api("com.github.andreypfau:kotlinio-crypto:+")
                }
            }
            val commonTest by getting {
                dependencies {
                    implementation(kotlin("test"))
                }
            }
        }
    }
}
