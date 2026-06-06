pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // OSMDroid is published on Maven Central, but keep JitPack as a fallback
        // for some OpenStreetMap related transitive artifacts.
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "RouteWake"
include(":app")
