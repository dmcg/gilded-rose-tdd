import org.jooq.meta.jaxb.ForcedType
import org.jooq.meta.jaxb.Property
import org.jooq.util.jaxb.tools.XMLAppendable

plugins {
    java
    id("org.jetbrains.kotlin.jvm") version "2.0.21"
    id("nu.studer.jooq") version "9.0"
    id("org.flywaydb.flyway") version "9.22.3" // 10.x gives issues connecting
    id("com.github.ben-manes.versions") version "0.51.0"
}

group = "com.gildedrose"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

val junitVersion = "5.11.3"
val jacksonVersion = "2.18.0"
val pgVersion = "42.7.4"
val testJdbcUrl = providers.environmentVariable("JDBC_URL").orElse("jdbc:postgresql://localhost:5433/gilded-rose").get()
val databaseUsername = providers.environmentVariable("DB_USERNAME").orElse("gilded").get()
val databasePassword = providers.environmentVariable("DB_PASSWORD").orElse("rose").get()


dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("dev.forkhandles:result4k:2.20.0.0")
    implementation("org.slf4j:slf4j-jdk14:2.0.16")

    implementation(platform("org.http4k:http4k-bom:5.32.4.0"))
    implementation("org.http4k:http4k-core")
    implementation("org.http4k:http4k-server-undertow")
    implementation("org.http4k:http4k-cloudnative")
    implementation("org.http4k:http4k-client-apache")

    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-parameter-names:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")

    implementation("org.postgresql:postgresql:$pgVersion")
    implementation("com.zaxxer:HikariCP:6.0.0")

    implementation("org.jooq:jooq:3.19.14")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.11.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("io.strikt:strikt-core:0.35.1")

    testImplementation("com.microsoft.playwright:playwright:1.48.0")

    testImplementation("org.http4k:http4k-testing-approval")
    testImplementation("org.http4k:http4k-testing-hamkrest")
    testImplementation("org.http4k:http4k-testing-strikt")

    jooqGenerator("org.postgresql:postgresql:$pgVersion")
}

tasks.test {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-receivers")
    }
}

flyway {
    url = testJdbcUrl
    user = databaseUsername
    password = databasePassword
}

private operator fun <T: XMLAppendable> T.invoke(block: T.() -> Unit) = apply(block)

jooq {
    edition = nu.studer.gradle.jooq.JooqEdition.OSS // default (can be omitted)
    configurations {
        create("main") { // name of the jOOQ configuration
            generateSchemaSourceOnCompilation.set(true) // default (can be omitted)
            jooqConfiguration {
                logging = org.jooq.meta.jaxb.Logging.WARN
                jdbc {
                    driver = "org.postgresql.Driver"
                    url = testJdbcUrl
                    user = databaseUsername
                    password = databasePassword
                    properties.add(Property().apply {
                        key = "ssl"
                        value = "false"
                    })
                }
                generator {
                    name = "org.jooq.codegen.DefaultGenerator"
                    database {
                        name = "org.jooq.meta.postgres.PostgresDatabase"
                        inputSchema = "public"
                        forcedTypes.addAll(
                            listOf(
                                ForcedType().apply {
                                    name = "instant"
                                    includeExpression = ".*"
                                    includeTypes = "TIMESTAMPTZ"
                                }
                            )
                        )
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
    allInputsDeclared = true
}
