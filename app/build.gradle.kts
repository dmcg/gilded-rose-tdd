plugins {
    id("kotlin-conventions")
    id("java-test-fixtures")
    alias(libs.plugins.flyway)
    alias(libs.plugins.jooq)
}

val testJdbcUrl = providers.environmentVariable("JDBC_URL").orElse("jdbc:postgresql://localhost:5433/gilded-rose").get()
val databaseUsername = providers.environmentVariable("DB_USERNAME").orElse("gilded").get()
val databasePassword = providers.environmentVariable("DB_PASSWORD").orElse("rose").get()

dependencies {
    api(project(":core"))
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

    implementation(libs.postgresql)
    implementation(libs.hikaricp)

    implementation(libs.jooq)
    implementation(libs.kotlinx.html)

    testFixturesImplementation(libs.junit.api)
    testImplementation(libs.junit.params)
    testImplementation(libs.junit.engine)
    testImplementation("org.junit.platform:junit-platform-launcher:1.11.3")

    testFixturesApi(kotlin("test"))
    testImplementation(libs.strikt)

    testImplementation(libs.http4k.testing.approval)
    testImplementation(libs.http4k.testing.hamkrest)
    testImplementation(libs.http4k.testing.strikt)
    testImplementation("io.github.classgraph:classgraph:4.8.162")

    jooqGenerator(libs.postgresql)
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

tasks.named("flywayMigrate") {
    inputs.files("src/main/resources/db/migration")
    outputs.dir("build/generated-src/jooq/main")
}

tasks.named<nu.studer.gradle.jooq.JooqGenerate>("generateJooq") {
    dependsOn("flywayMigrate")
    inputs.files("src/main/resources/db/migration")
    allInputsDeclared = true
}
