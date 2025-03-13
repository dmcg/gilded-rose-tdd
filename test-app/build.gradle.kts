plugins {
    id("kotlin-conventions")
    alias(libs.plugins.versions)
    alias(libs.plugins.taskinfo)
}

dependencies {
    testImplementation(project(":app"))
    testImplementation(testFixtures(project(":app")))
    testImplementation(libs.playwright)
}
