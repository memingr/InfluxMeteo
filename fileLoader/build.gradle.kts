import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.0"
    id("com.microsoft.azure.azurefunctions") version "1.8.2"
    application
}

group = "me.memingr"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("com.influxdb:influxdb-client-kotlin:3.1.0")
    implementation("com.microsoft.azure.functions:azure-functions-java-library:1.4.2")

}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}

application {
    mainClass.set("MainKt")
}

azurefunctions {
    resourceGroup = "java-functions-group"
    appName = "Letadlo-functions"
    pricingTier = "Consumption"
    region = "westus"
    /*
    setRuntime(closureOf<com.microsoft.azure.gradle.configuration.GradleRuntimeConfig> {
        os("Linux")
    })

     */
    setAuth(closureOf<com.microsoft.azure.gradle.auth.GradleAuthConfig> {
        type = "azure_cli"
    })
    localDebug = "transport=dt_socket,server=y,suspend=n,address=5005"
}
