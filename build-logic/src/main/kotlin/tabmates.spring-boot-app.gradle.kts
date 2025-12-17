plugins {
    id("tabmates.spring-boot-service")
    id("org.springframework.boot")
    kotlin("plugin.spring")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(libraries.findVersion("jvm").get().requiredVersion.toInt())
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}