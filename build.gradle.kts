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
    val nativeTargets = ArrayList<String>()
//    if (HostManager.hostIsMac) {
//        nativeTargets.add(macosX64().name)
//        nativeTargets.add(macosArm64().name)
//    }
//    nativeTargets.add(linuxX64().name)
//    nativeTargets.add(mingwX64().name)

    sourceSets {
        val nativeMain by creating {
            dependsOn(commonMain.get())
        }
        val nativeTest by creating {
            dependsOn(commonTest.get())
        }
        nativeTargets.forEach {
            getByName("${it}Main").dependsOn(nativeMain)
            getByName("${it}Test").dependsOn(nativeTest)
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
            url = uri("https://maven.pkg.github.com/andreypfau/kotlinio")
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
