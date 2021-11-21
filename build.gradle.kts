import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.31"
    kotlin("kapt") version "1.5.31"
    kotlin("plugin.serialization") version "1.5.31"
    id("com.github.johnrengelman.shadow") version "7.1.0"
}

group = "com.github.miong"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.eclipse.org/content/repositories/paho-releases/")
}

dependencies {
    val exposedVersion = "0.34.1"
    val pahoVersion = "1.2.0"

    testImplementation(kotlin("test"))

    implementation("org.slf4j:slf4j-api:1.7.25")
    implementation("org.slf4j:slf4j-simple:1.6.1")

    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.xerial:sqlite-jdbc:3.30.1")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:$pahoVersion")

    implementation("com.github.miong:CardsOfLegendsMessages:main-SNAPSHOT")
}

tasks.jar {
    manifest {
        attributes(mapOf("Main-Class" to "com.bubul.col.server.MainKt"))
    }
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}