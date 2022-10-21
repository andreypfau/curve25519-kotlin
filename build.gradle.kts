import org.jetbrains.kotlin.konan.target.HostManager

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
    val darwinTargets = if (HostManager.hostIsMac) {
        listOf(
            macosArm64().name,
            macosX64().name
        )
    } else emptyList()
    val linuxTargets = listOf(
        linuxX64().name,
        linuxArm64().name
    )
    val mingwTargets = listOf(
        mingwX64().name
    )
    val nativeTargets = darwinTargets + linuxTargets + mingwTargets

    sourceSets {
        val nativeMain by creating {
            dependsOn(commonMain.get())
        }
        nativeTargets.forEach {
            getByName("${it}Main").dependsOn(nativeMain)
        }

        val darwinMain by creating {
            dependsOn(nativeMain)
        }
        darwinTargets.forEach {
            getByName("${it}Main").dependsOn(darwinMain)
        }

        val linuxMain by creating {
            dependsOn(nativeMain)
        }
        linuxTargets.forEach {
            getByName("${it}Main").dependsOn(linuxMain)
        }

        val mingwMain by creating {
            dependsOn(nativeMain)
        }
        mingwTargets.forEach {
            getByName("${it}Main").dependsOn(mingwMain)
        }
    }

    sourceSets {
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                api("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:0.4.5")
            }
        }
    }
}

benchmark {
    targets {
        register("jvmTest")
        register("nativeTest")
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/andreypfau/curve25519-kotlin")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
        publications {
            create<MavenPublication>("maven") {
                from(components["kotlin"])
            }
        }
    }
}
