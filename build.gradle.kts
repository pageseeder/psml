plugins {
  id("java-library")
  id("maven-publish")
  alias(libs.plugins.jreleaser)
}

val title: String by project
val gitName: String by project
val website: String by project

group = "org.pageseeder"
version = file("version.txt").readText().trim()
description = findProperty("description") as String?

repositories {
  mavenCentral()
}

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

  testImplementation(platform(libs.junit.bom))
  testImplementation(libs.bundles.junit)
  testImplementation(libs.hamcrest)
  testImplementation(libs.xmlunit.core)
  testImplementation(libs.xmlunit.matchers)
  testImplementation(libs.commons.io)
  testImplementation(libs.annotations)

  testRuntimeOnly(libs.junit.jupiter.engine)
  testRuntimeOnly(libs.slf4j.simple)
  testRuntimeOnly (libs.saxon.he)
  testRuntimeOnly(libs.rhino.engine) {
    because("Required by TeX/AsciiMath conversion in Java 15+")
  }
}

tasks.wrapper {
  gradleVersion = "8.13"
  distributionType = Wrapper.DistributionType.BIN
}

tasks.test {
  useJUnitPlatform()
}

tasks.withType<Javadoc> {
  options {
    encoding = "UTF-8"
  }
}

publishing {
  publications {
    create<MavenPublication>("maven") {
      from(components["java"])
      pom {
        name.set(title)
        description.set(project.description)
        url.set(website)
        licenses {
          license {
            name.set("The Apache Software License, Version 2.0")
            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
          }
        }
        organization {
          name.set("Allette Systems")
          url.set("https://www.allette.com.au")
        }
        scm {
          url.set("git@github.com:pageseeder/${gitName}.git")
          connection.set("scm:git:git@github.com:pageseeder/${gitName}.git")
          developerConnection.set("scm:git:git@github.com:pageseeder/${gitName}.git")
        }
        developers {
          developer {
            name.set("Alberto Santos")
            email.set("asantos@allette.com.au")
          }
          developer {
            name.set("Christophe Lauret")
            email.set("clauret@weborganic.com")
          }
          developer {
            name.set("Jean-Baptiste Reure")
            email.set("jbreure@weborganic.com")
          }
          developer {
            name.set("Philip Rutherford")
            email.set("philipr@weborganic.com")
          }
        }
      }
    }
  }
  repositories {
    maven {
      url = layout.buildDirectory.dir("staging-deploy").get().asFile.toURI()
    }
  }
}

jreleaser {
  configFile.set(file("jreleaser.toml"))
}
