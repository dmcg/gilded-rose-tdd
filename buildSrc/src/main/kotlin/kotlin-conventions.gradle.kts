plugins {
    java
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

tasks.test {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
