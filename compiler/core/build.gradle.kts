import java.io.FileInputStream
import java.io.FileNotFoundException
import java.net.URI
import java.util.*

plugins {
  `maven-publish`
  alias(libs.plugins.org.jetbrains.kotlin.jvm)
  alias(libs.plugins.ksp)
  id("stock.me.di.merge-tests")
}

group = "in.stock.me"
version = "1.0.0"

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
      artifactId = "di-core"
      from(components["kotlin"])
    }
  }
}

dependencies {
  api(libs.ksp.processor.api)
  api(libs.dagger)
  api(libs.kotlin.poet)
  ksp(libs.dagger.compiler)
  implementation(libs.ksp.testing)
}

tasks.withType<Test> {
  useJUnitPlatform()
}

tasks.test {
  useJUnitPlatform()
}
kotlin {
  jvmToolchain(17)
}