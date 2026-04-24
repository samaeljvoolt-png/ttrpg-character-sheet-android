pluginManagement {
    repositories {
        google()
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

rootProject.name = "TtrpgCharacterSheet"

include(":app")
include(":core:ui")
include(":core:domain")
include(":core:data")
include(":core:systems-api")
include(":feature:charactersheet")
include(":feature:systems")
include(":domain:common")
include(":domain:dnd5e")
