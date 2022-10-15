import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.kotlinx.benchmark") version "0.4.5"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.7.20"
    `maven-publish`
}

group = "com.github.andreypfau"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
}

allOpen {
    annotation("org.openjdk.jmh.annotations.State")
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
    if (HostManager.host == KonanTarget.MACOS_X64) macosX64("native")
    if (HostManager.host == KonanTarget.MACOS_ARM64) macosArm64("native")
    if (HostManager.hostIsLinux) linuxX64("native")
    if (HostManager.hostIsMingw) mingwX64("native")

    sourceSets {
        val commonMain by getting {
            dependencies {
                api("com.github.andreypfau:kotlinio-crypto:+")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                api("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:0.4.5")
            }
        }
        val nativeTest by getting {
            dependsOn(commonTest)
        }
    }
}

benchmark {
    targets {
        register("jvmTest")
        register("nativeTest")
    }
}
