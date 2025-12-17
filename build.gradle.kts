plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.spring) apply false
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
    alias(libs.plugins.kotlin.jpa) apply false
}

group = "de.tabmates"
version = "0.0.1-SNAPSHOT"
description = "TabMates Server"

subprojects {
    group = rootProject.group
    version = rootProject.version
}