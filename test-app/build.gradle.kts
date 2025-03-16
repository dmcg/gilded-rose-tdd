plugins {
    id("kotlin-conventions")
}

dependencies {
    testImplementation(project(":app"))
    testImplementation(testFixtures(project(":app")))
    testImplementation(libs.playwright)
}
