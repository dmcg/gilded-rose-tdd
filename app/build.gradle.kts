plugins {
    id("kotlin-conventions")
    id("java-test-fixtures")
}

dependencies {
    api(project(":core"))
    implementation(project(":database"))
    implementation(libs.kotlinx.coroutines)
    implementation(libs.slf4j)

    api(platform(libs.http4k.bom))
    api(libs.http4k.core)
    implementation(libs.http4k.server.undertow)
    implementation(libs.http4k.config)
    implementation(libs.http4k.client.apache)

    implementation(libs.jackson.databind)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.jackson.module.parameter.names)
    implementation(libs.jackson.datatype.jdk8)
    implementation(libs.jackson.datatype.jsr310)

    implementation(libs.kotlinx.html)

    testFixturesImplementation(libs.junit.api)
    testFixturesImplementation(testFixtures(project(":core")))
    testFixturesApi(kotlin("test"))

    testImplementation(libs.junit.engine)
    testImplementation("org.junit.platform:junit-platform-launcher:1.11.3")

    testImplementation(testFixtures(project(":core")))
    testImplementation(libs.strikt)

    testImplementation(libs.http4k.testing.approval)
    testImplementation(libs.http4k.testing.hamkrest)
    testImplementation(libs.http4k.testing.strikt)
    testImplementation("io.github.classgraph:classgraph:4.8.162")
}

