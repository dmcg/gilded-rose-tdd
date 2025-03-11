plugins {
    id("kotlin-conventions")
    alias(libs.plugins.versions)
    alias(libs.plugins.taskinfo)
}

group = "com.gildedrose"
version = "0.0.1-SNAPSHOT"

dependencies {
    testImplementation(project(":app"))
    testImplementation(testFixtures(project(":app")))
    testImplementation(libs.playwright)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-receivers")
        suppressWarnings = true // TODO 2024-11-13 DMCG remove me
    }
}
