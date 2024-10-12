import MyTargetConfigurations.Server.serverConfig

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ktor)
    application
}

group = MyConstants.ROOT_PACKAGE
version = MyTargetConfigurations.Server.VERSION
application {
    serverConfig()()
}

dependencies {
    implementation(projects.shared)

    // ktor
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.contentnegotiation)
    implementation(libs.ktor.server.websockets)
    implementation(libs.ktor.serialization.json)

    // serial communication
    implementation(libs.jSerialComm)

    // webcam
    implementation(libs.webcam.capture)

    // logging
    implementation(libs.logback)

    // Tests
    testImplementation(libs.ktor.server.tests)
    testImplementation(libs.kotlin.test.junit)
}
