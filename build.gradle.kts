import org.jetbrains.kotlin.konan.target.HostManager

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.kotlinx.benchmark") version "0.4.5"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.7.20"
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    `maven-publish`
    signing
}

group = "io.github.andreypfau"
version = "0.0.2"

repositories {
    mavenLocal()
    mavenCentral()
}

allOpen {
    annotation("org.openjdk.jmh.annotations.State")
}

kotlin {
    val isCI = System.getenv("CI") == "true"
    val isCIMacOS = isCI && HostManager.hostIsMac
    if (!isCIMacOS) {
        jvm {
            withJava()
            compilations.all {
                kotlinOptions.jvmTarget = "1.8"
            }
            testRuns["test"].executionTask.configure {
                useJUnitPlatform()
            }
        }
        // TODO: Fix tests fo JS
//        js {
//            nodejs()
//            browser()
//        }
    }
    val darwinTargets = if (HostManager.hostIsMac) {
        listOf(
            macosArm64().name,
            macosX64().name
        )
    } else emptyList()
    val linuxTargets = if (!isCIMacOS) {
        listOf(
            linuxX64().name,
            linuxArm64().name
        )
    } else emptyList()
    val mingwTargets = if (!isCIMacOS) {
        listOf(
            mingwX64().name
        )
    } else emptyList()
    val nativeTargets = darwinTargets + linuxTargets + mingwTargets
    println("Native targets: \n${nativeTargets.joinToString("\n* ", prefix = "* ")}")

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

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

publishing {
    publications {
        withType<MavenPublication> {
            artifact(javadocJar.get())
            pom {
                name.set("curve25519-kotlin")
                description.set("A pure Kotlin implementation of group operations on Curve25519.")
                url.set("https://github.com/andreypfau/curve25519-kotlin")
                licenses {
                    license {
                        name.set("GNU General Public License v3.0")
                        url.set("https://www.gnu.org/licenses/gpl-3.0.en.html")
                    }
                }
                developers {
                    developer {
                        id.set("andreypfau")
                        name.set("Andrey Pfau")
                        email.set("andreypfau@ton.org")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/andreypfau/curve25519-kotlin.git")
                    developerConnection.set("scm:git:ssh://github.com/andreypfau/curve25519-kotlin.git")
                    url.set("https://github.com/andreypfau/curve25519-kotlin")
                }
            }
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/andreypfau/curve25519-kotlin")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

nexusPublishing {
    repositories {
        sonatype {
            username.set(project.findProperty("ossrhUsername") as? String ?: System.getenv("OSSRH_USERNAME"))
            password.set(project.findProperty("ossrhPassword") as? String ?: System.getenv("OSSRH_PASSWORD"))
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
        }
    }
}

signing {
    val secretKey = project.findProperty("signing.secretKey") as? String ?: System.getenv("SIGNING_SECRET_KEY")
    val password = project.findProperty("signing.password") as? String ?: System.getenv("SIGNING_PASSWORD")
    isRequired = secretKey != null && password != null
    useInMemoryPgpKeys(secretKey, password)
    sign(publishing.publications)
}
