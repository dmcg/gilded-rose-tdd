plugins {
    id("kotlin-conventions")
    alias(libs.plugins.flyway)
    alias(libs.plugins.jooq)
}

val testJdbcUrl = providers.environmentVariable("JDBC_URL").orElse("jdbc:postgresql://localhost:5433/gilded-rose").get()
val databaseUsername = providers.environmentVariable("DB_USERNAME").orElse("gilded").get()
val databasePassword = providers.environmentVariable("DB_PASSWORD").orElse("rose").get()


dependencies {
    implementation(project(":core"))
    testImplementation(testFixtures(project(":core")))

    api(libs.jooq)
    implementation(libs.postgresql)
    implementation(libs.hikaricp)

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
