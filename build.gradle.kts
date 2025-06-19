plugins {
  `java-library`
  `maven-publish`
  id("io.codearte.nexus-staging") version "0.30.0"
}

group = "org.pageseeder"
version = file("version.txt").readText().trim()
description = project.findProperty("title") as? String ?: "Default Title"

apply(from = "gradle/publish-mavencentral.gradle")

repositories {
  mavenCentral {
    url = uri("https://maven-central.storage.googleapis.com/maven2")
  }
  maven {
    url = uri("https://s01.oss.sonatype.org/content/groups/public/")
  }
}


// Use Java toolchain to ensure the correct JVM is used by Gradle
java {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(11))
  }
  withJavadocJar()
  withSourcesJar()
}

dependencies {
  compileOnly(libs.annotations)

  api(libs.slf4j.api)
  api(libs.pso.xmlwriter)
  api(libs.pso.diffx)

  testImplementation(libs.junit)
  testImplementation(libs.slf4j.simple)
  testImplementation(libs.hamcrest.java)
  testImplementation(libs.hamcrest.junit)
  testImplementation(libs.xmlunit.core)
  testImplementation(libs.xmlunit.matchers)
  testImplementation(libs.commons.io)
  testImplementation(libs.annotations)

  testRuntimeOnly (libs.saxon.he)
  testRuntimeOnly(libs.rhino.engine) {
    because("Required by TeX/AsciiMath conversion in Java 15+")
  }

}
