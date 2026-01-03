import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("io.spring.dependency-management")
    id("tabmates.ktlint")
}

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:${libraries.findVersion("spring-boot").get()}")
    }
}

val jvmVersion = libraries.findVersion("jvm").get().requiredVersion

configure<KotlinJvmProjectExtension> {
    jvmToolchain(jvmVersion.toInt())
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
        jvmTarget = JvmTarget.fromTarget(jvmVersion)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
