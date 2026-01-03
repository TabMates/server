plugins {
    id("org.jlleitschuh.gradle.ktlint")
}

ktlint {
    version.set(libraries.findVersion("ktlint-version").get().requiredVersion)
    android.set(false)
    enableExperimentalRules.set(true)
}

