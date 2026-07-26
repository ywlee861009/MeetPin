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

rootProject.name = "MeetPin"
include(":app")

// Core modules
include(":core:model")
include(":core:domain")
include(":core:designsystem")
include(":core:network")
include(":core:data")
include(":core:location")

// Feature modules
include(":feature:map")
include(":feature:lobby")
include(":feature:tracking")
