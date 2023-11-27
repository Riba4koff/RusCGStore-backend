
val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val koin_version: String by project

val exposed_version: String by project
val h2_version: String by project
plugins {
    kotlin("jvm") version "1.9.21"
    id("io.ktor.plugin") version "2.3.6"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.21"
}

group = "ru.cgstore"
version = "0.0.1"

application {
    mainClass.set("io.ktor.server.cio.EngineMain")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    // server core
    implementation("io.ktor:ktor-server-core-jvm")

    // websockets
    implementation("io.ktor:ktor-server-websockets-jvm")

    // serialization
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")

    // content negotiation
    implementation("io.ktor:ktor-server-content-negotiation-jvm")

    // exposed
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("com.h2database:h2:$h2_version")

    // call logging
    implementation("io.ktor:ktor-server-call-logging-jvm")

    // swagger
    implementation("io.ktor:ktor-server-swagger-jvm")

    // resources
    implementation("io.ktor:ktor-server-resources")

    //apache.common.codec
    implementation("commons-codec:commons-codec:1.13")

    // authentication
    implementation("io.ktor:ktor-server-auth-jvm")
    implementation("io.ktor:ktor-server-auth-jwt-jvm")
    implementation("io.ktor:ktor-client-core-jvm")
    implementation("io.ktor:ktor-client-apache-jvm")

    // sessions
    implementation("io.ktor:ktor-server-sessions-jvm")

    // CIO
    implementation("io.ktor:ktor-server-cio-jvm")

    // logback
    implementation("ch.qos.logback:logback-classic:$logback_version")

    // tests
    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")

    // dependency injection
    implementation("io.insert-koin:koin-ktor:$koin_version")
    implementation("io.insert-koin:koin-core:$koin_version")
}
