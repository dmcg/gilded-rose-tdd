plugins {
    id("kotlin-conventions")
    id("java-test-fixtures")
}

dependencies {
    api(libs.result4k)
    api(project(":foundation"))
    testFixturesApi(libs.junit.api)
    testFixturesApi(kotlin("test"))
    testFixturesApi(libs.junit.params)
}
