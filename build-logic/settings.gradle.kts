dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("kotlin", "1.9.23")

            library("kotlin-gradle-plugin", "org.jetbrains.kotlin", "kotlin-gradle-plugin").versionRef("kotlin")
        }
    }
}
