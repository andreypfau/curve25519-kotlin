plugins {
    kotlin("multiplatform")
    id("org.jetbrains.kotlinx.benchmark") version "0.4.5"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.7.20"
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    `maven-publish`
    signing
}

group = "io.github.andreypfau"
version = "0.0.1"
val isReleaseVersion = !version.toString().endsWith("SNAPSHOT")

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
//    val darwinTargets = if (HostManager.hostIsMac) {
//        listOf(
//            macosArm64().name,
//            macosX64().name
//        )
//    } else emptyList()
//    val linuxTargets = listOf(
//        linuxX64().name,
//        linuxArm64().name
//    )
//    val mingwTargets = listOf(
//        mingwX64().name
//    )
//    val nativeTargets = darwinTargets + linuxTargets + mingwTargets
//
//    sourceSets {
//        val nativeMain by creating {
//            dependsOn(commonMain.get())
//        }
//        nativeTargets.forEach {
//            getByName("${it}Main").dependsOn(nativeMain)
//        }
//
//        val darwinMain by creating {
//            dependsOn(nativeMain)
//        }
//        darwinTargets.forEach {
//            getByName("${it}Main").dependsOn(darwinMain)
//        }
//
//        val linuxMain by creating {
//            dependsOn(nativeMain)
//        }
//        linuxTargets.forEach {
//            getByName("${it}Main").dependsOn(linuxMain)
//        }
//
//        val mingwMain by creating {
//            dependsOn(nativeMain)
//        }
//        mingwTargets.forEach {
//            getByName("${it}Main").dependsOn(mingwMain)
//        }
//    }

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
    publications {
        create<MavenPublication>("main") {
            from(components["kotlin"])
            pom {
                name.set("curve25519-kotlin")
                description.set("CA pure Kotlin implementation of group operations on Curve25519.")
                url.set("https://github.com/andreypfau/curve25519-kotlin")
            }
        }
    }
//        maven {
//            name = "GitHubPackages"
//            url = uri("https://maven.pkg.github.com/andreypfau/curve25519-kotlin")
//            credentials {
//                username = System.getenv("GITHUB_ACTOR")
//                password = System.getenv("GITHUB_TOKEN")
//            }
//        }
}

nexusPublishing {
    repositories {
        create("OSSRH") {
            username.set(project.findProperty("ossrhUsername") as? String ?: System.getenv("OSSRH_USERNAME"))
            password.set(project.findProperty("ossrhPassword") as? String ?: System.getenv("OSSRH_PASSWORD"))
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
        }
    }
}

signing {
    val keyId = project.findProperty("signing.keyId") as? String ?: System.getenv("SIGNING_KEY_ID")
    val secretKey = project.findProperty("signing.secretKey") as? String ?: System.getenv("SIGNING_SECRET_KEY")
    val password = project.findProperty("signing.password") as? String ?: System.getenv("SIGNING_PASSWORD")
    isRequired = nexusPublishing.useStaging.get() && keyId != null && secretKey != null && password != null
    useInMemoryPgpKeys(
        keyId,
        secretKey,
        password,
    )
    sign(publishing.publications["main"])
}
