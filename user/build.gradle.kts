plugins {
    id("java-library")
    id("tabmates.spring-boot-service")
    kotlin("plugin.jpa")
}

dependencies {
    implementation(projects.common)

    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.data.redis)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.web)

    implementation(libs.jwt.api)
    runtimeOnly(libs.jwt.impl)
    runtimeOnly(libs.jwt.jackson)

    runtimeOnly(libs.postgresql)
}
