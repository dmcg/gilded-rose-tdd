plugins {
    id("kotlin-conventions")
}

dependencies {
    testImplementation(project(":app"))
    testImplementation(libs.http4k.client.apache)

    testImplementation(libs.kotlinx.coroutines)
    testImplementation(libs.result4k)
    testImplementation(libs.jackson.databind)
    testImplementation(libs.jackson.module.kotlin)
    testImplementation(libs.jackson.module.parameter.names)
    testImplementation(libs.jackson.datatype.jdk8)
    testImplementation(libs.jackson.datatype.jsr310)

    testImplementation(libs.http4k.testing.approval)
    testImplementation(libs.strikt)
}
