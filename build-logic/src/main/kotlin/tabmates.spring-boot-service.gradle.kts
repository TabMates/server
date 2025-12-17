plugins {
    id("tabmates.kotlin-common")
}

dependencies {
    "implementation"(libraries.findLibrary("kotlin-reflect").get())
    "implementation"(libraries.findLibrary("kotlin-stdlib").get())
    "implementation"(libraries.findLibrary("spring-boot-starter-web").get())

    "testImplementation"(libraries.findLibrary("spring-boot-starter-test").get())
    "testImplementation"(libraries.findLibrary("kotlin-test-junit5").get())
    "testImplementation"(libraries.findLibrary("junit-platform-launcher").get())
}