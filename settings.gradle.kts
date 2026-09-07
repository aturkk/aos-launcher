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
    }
}

rootProject.name = "AOS Launcher"

// Core modülleri
include(":core:common")
include(":core:domain")
include(":core:data")
include(":core:ui")

// Feature modülleri
include(":feature:home")
include(":feature:appdrawer")
include(":feature:settings")

// App modülü
include(":app")
