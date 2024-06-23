import java.io.FileInputStream
import java.io.FileNotFoundException
import java.net.URI
import java.util.*

plugins {
  alias(libs.plugins.org.jetbrains.kotlin.jvm)
  `maven-publish`
  alias(libs.plugins.ksp)
}

group = "in.stock.me"
version = "1.0.0-SNAPSHOT"

publishing {
  publications {
    repositories {
      maven {
        url = URI.create("https://maven.pkg.jetbrains.space/stockme/p/main/stock-me-android")

        credentials {
          val repoUsername = "REPO_USERNAME"
          val repoToken = "TOKEN"
          val properties = Properties().apply {
            try {
              load(FileInputStream(File("${rootDir.absolutePath}/local.properties")))
            } catch (e: FileNotFoundException) {
              put(repoUsername, System.getenv(repoUsername) ?: "")
              put(repoToken, System.getenv(repoToken) ?: "")
            }
          }
          username = properties[repoUsername].toString()
          password = properties[repoToken].toString()
        }
      }
    }
    create<MavenPublication>("maven") {
      artifactId = "di-compiler"
    }
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
}

kotlin {
  jvmToolchain(17)
}

dependencies {
  implementation(libs.di.runtime)
  ksp(libs.dagger.compiler)
  implementation(projects.compiler.core)

  testImplementation(libs.ksp.testing)
  testImplementation(libs.koTest)
}