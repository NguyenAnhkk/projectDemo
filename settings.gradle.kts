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
        maven("https://storage.zego.im/maven")   // <- Add this line.
        maven("https://www.jitpack.io")  // <- Add this line.
    }
}

rootProject.name = "ProjectDemo"
include(":app")
 