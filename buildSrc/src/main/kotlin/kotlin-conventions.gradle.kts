plugins {
    java
    alias(libs.plugins.kotlin.jvm)
}

group = "com.gildedrose"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.junit.api)
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-receivers")
        suppressWarnings = true // TODO 2024-11-13 DMCG remove me
    }
}
