plugins {
    java
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.flyway)
    alias(libs.plugins.jooq)
    alias(libs.plugins.versions)
    alias(libs.plugins.taskinfo)
}

group = "com.gildedrose"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

val testJdbcUrl = providers.environmentVariable("JDBC_URL").orElse("jdbc:postgresql://localhost:5433/gilded-rose").get()
val databaseUsername = providers.environmentVariable("DB_USERNAME").orElse("gilded").get()
val databasePassword = providers.environmentVariable("DB_PASSWORD").orElse("rose").get()

dependencies {
    implementation(libs.kotlinx.coroutines)
    implementation(libs.result4k)
    implementation(libs.slf4j)

    implementation(platform(libs.http4k.bom))
    implementation(libs.http4k.core)
    implementation(libs.http4k.server.undertow)
    implementation(libs.http4k.config)
    implementation(libs.http4k.client.apache)

    implementation(libs.jackson.databind)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.jackson.module.parameter.names)
    implementation(libs.jackson.datatype.jdk8)
    implementation(libs.jackson.datatype.jsr310)

    implementation(libs.postgresql)
    implementation(libs.hikaricp)

    implementation(libs.jooq)
    implementation(libs.kotlinx.html)

    testImplementation(libs.junit.api)
    testImplementation(libs.junit.params)
    testImplementation(libs.junit.engine)

    testImplementation(kotlin("test"))
    testImplementation(libs.strikt)

    testImplementation(libs.playwright)

    testImplementation(libs.http4k.testing.approval)
    testImplementation(libs.http4k.testing.hamkrest)
    testImplementation(libs.http4k.testing.strikt)
    testImplementation("org.testcontainers:postgresql:1.19.8")
    testImplementation("org.flywaydb:flyway-core:10.21.0")
    testImplementation("org.flywaydb:flyway-database-postgresql:10.21.0")

    jooqGenerator(libs.postgresql)
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

flyway {
    url = testJdbcUrl
    user = databaseUsername
    password = databasePassword
}

@Suppress("UnstableApiUsage") // XMLAppendable
private operator fun <T : org.jooq.util.jaxb.tools.XMLAppendable> T.invoke(block: T.() -> Unit) = apply(block)

jooq {
    edition = nu.studer.gradle.jooq.JooqEdition.OSS // default (can be omitted)
    configurations {
        create("main") {
            generateSchemaSourceOnCompilation = true

            jooqConfiguration {
                logging = org.jooq.meta.jaxb.Logging.WARN
                jdbc {
                    driver = "org.postgresql.Driver"
                    url = testJdbcUrl
                    user = databaseUsername
                    password = databasePassword
                    properties.add(org.jooq.meta.jaxb.Property().withKey("ssl").withValue("false"))
                }
                generator {
                    name = "org.jooq.codegen.DefaultGenerator"
                    database {
                        name = "org.jooq.meta.postgres.PostgresDatabase"
                        inputSchema = "public"
                        forcedTypes.add((org.jooq.meta.jaxb.ForcedType()) {
                            name = "instant"
                            includeExpression = ".*"
                            includeTypes = "TIMESTAMPTZ"
                        })
                    }
                    generate {
                        isDeprecated = false
                        isRecords = true
                        isImmutablePojos = true
                        isFluentSetters = true
                    }
                    target {
                        packageName = "com.gildedrose.db"
                        directory = "build/generated-src/jooq/main" // default (can be omitted)
                    }
                    strategy.name = "org.jooq.codegen.DefaultGeneratorStrategy"
                }
            }
        }
    }
}

tasks.named<nu.studer.gradle.jooq.JooqGenerate>("generateJooq") {
    dependsOn("flywayMigrate")
    inputs.files("src/main/resources/db/migration")
    allInputsDeclared = true
}
