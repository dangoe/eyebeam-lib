/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    kotlin("jvm") version "1.9.22"
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("javax.xml.bind:jaxb-api:2.3.1")
    implementation("commons-io:commons-io:2.15.1")
    implementation("org.apache.commons:commons-lang3:3.14.0")
    implementation("org.apache.commons:commons-compress:1.26.0")
    implementation("org.slf4j:slf4j-api:2.0.12")
    implementation("com.drewnoakes:metadata-extractor:2.19.0")
    implementation("com.google.code.gson:gson:2.9.1")
    implementation("io.arrow-kt:arrow-core:1.2.0")
}

group = "de.maci.photography"
version = "1.1.1-SNAPSHOT"
description = "eyebeam-lib"