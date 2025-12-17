plugins {
    id("tabmates.spring-boot-app")
}

dependencies {
    implementation(libs.kotlin.reflect)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.actuator)

    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.mail)
    implementation(libs.spring.boot.starter.amqp)
    implementation(libs.spring.boot.starter.data.redis)
    runtimeOnly(libs.postgresql)
}
