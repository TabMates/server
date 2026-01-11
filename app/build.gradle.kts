plugins {
    id("tabmates.spring-boot-app")
}

dependencies {
    implementation(projects.common)
    implementation(projects.notification)
    implementation(projects.tabgroup)
    implementation(projects.user)

    implementation(libs.kotlin.reflect)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.amqp)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.data.redis)
    implementation(libs.spring.boot.starter.mail)
    implementation(libs.spring.boot.starter.security)

    runtimeOnly(libs.postgresql)
}
