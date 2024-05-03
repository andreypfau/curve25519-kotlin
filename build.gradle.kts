import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("publish")
}

group = "io.github.andreypfau"
version = "0.0.7"

repositories {
    mavenLocal()
    mavenCentral()
}

kotlin {
    jvm()

    iosArm64()
    iosSimulatorArm64()
    iosX64()

    macosArm64()
    macosX64()

    tvosArm64()
    tvosSimulatorArm64()
    tvosX64()

    watchosArm32()
    watchosArm64()
    watchosDeviceArm64()
    watchosSimulatorArm64()
    watchosX64()

    mingwX64()

    linuxArm64()
    linuxX64()

    js(IR) {
        nodejs()
        browser()
    }

    @OptIn(org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl::class)
    wasmJs {
        nodejs()
        browser()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.kotlinx.crypto.sha2)
                api(libs.kotlinx.crypto.subtle)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
        freeCompilerArgs.add("-opt-in=kotlin.ExperimentalUnsignedTypes")
        allWarningsAsErrors.set(true)
    }
}
