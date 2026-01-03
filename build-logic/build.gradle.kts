plugins {
    `kotlin-dsl`
    alias(libs.plugins.ktlint)
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation(libs.dependency.management.plugin)
    implementation(libs.kotlin.allopen)
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.ktlint.gradle.plugin)
    implementation(libs.spring.boot.gradle.plugin)
}

ktlint {
    version.set(libs.versions.ktlint.version.get())
    android.set(false)
    ignoreFailures.set(false)
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }
}
