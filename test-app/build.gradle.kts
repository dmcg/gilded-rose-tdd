plugins {
    java
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.versions)
    alias(libs.plugins.taskinfo)
}

group = "com.gildedrose"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(project(":app"))
    testImplementation(testFixtures(project(":app")))
    testImplementation(libs.junit.api)
    testImplementation(kotlin("test"))
    testImplementation(libs.playwright)
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
