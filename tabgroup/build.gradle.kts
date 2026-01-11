plugins {
    id("java-library")
    id("tabmates.spring-boot-service")
    kotlin("plugin.jpa")
}

dependencies {
    implementation(projects.common)

    implementation(libs.spring.boot.starter.amqp)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.websocket)

    runtimeOnly(libs.postgresql)
}
