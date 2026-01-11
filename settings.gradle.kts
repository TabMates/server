pluginManagement {
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "TabMatesServer"

include("app")
include("common")
include("notification")
include("tabgroup")
include("user")
