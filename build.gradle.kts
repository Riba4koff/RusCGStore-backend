val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val kotlinx_serialization_version: String by project
val koin_version: String by project
val swagger_codegen_version: String by project

val exposed_version: String by project
val h2_version: String by project

plugins {
    kotlin("jvm") version "1.9.10"
    id("io.ktor.plugin") version "2.3.7"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.10" apply true
}

group = "ru.cgstore"
version = "0.0.1"

application {
    //mainClass.set("io.ktor.server.cio.EngineMain")
    mainClass.set("ru.cgstore.ApplicationKt")
    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

ktor {
    fatJar {
        archiveFileName.set("ruscgstore.jar")
    }
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-auth-jvm")
    implementation("io.ktor:ktor-server-auth-jwt-jvm")

    // arrow core
    implementation("io.arrow-kt:arrow-core:1.2.0")
    implementation("io.arrow-kt:arrow-fx-coroutines:1.2.0")

    // sessions
    implementation("io.ktor:ktor-server-sessions-jvm")

    // call logging
    implementation("io.ktor:ktor-server-call-logging-jvm")

    // content negotiation
    implementation("io.ktor:ktor-server-content-negotiation-jvm")

    // serialization
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")
    implementation("io.ktor:ktor-serialization-kotlinx:$kotlinx_serialization_version")

    // swagger
    implementation("io.ktor:ktor-server-swagger-jvm")
    implementation("io.ktor:ktor-server-openapi:$ktor_version")
    implementation("io.ktor:ktor-server-swagger:$ktor_version")
    implementation("io.swagger.codegen.v3:swagger-codegen-generators:$swagger_codegen_version")

    // resources
    implementation("io.ktor:ktor-server-resources")

    //postgres
    implementation("org.postgresql:postgresql:42.2.2")

    //exposed
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-dao:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")

    implementation("com.h2database:h2:$h2_version")

    // logback
    implementation("ch.qos.logback:logback-classic:$logback_version")

    // tests
    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")

    //websockets
    implementation("io.ktor:ktor-server-websockets-jvm")

    implementation("io.ktor:ktor-server-cio-jvm")

    implementation("ch.qos.logback:logback-classic:$logback_version")

    testImplementation("io.ktor:ktor-server-tests-jvm")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")

    //apache.common.codec
    implementation("commons-codec:commons-codec:1.13")
    implementation("io.ktor:ktor-server-cors-jvm:2.3.6")
    //kotlinx.datetime
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.2.0")

    // Dependency injection
    implementation("io.insert-koin:koin-ktor:$koin_version")
    implementation("io.insert-koin:koin-logger-slf4j:$koin_version")
    implementation("io.insert-koin:koin-core:$koin_version")
}
