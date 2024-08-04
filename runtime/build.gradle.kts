import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.net.URI
import java.util.*

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.ksp)
  id("stock.me.di.merge-tests")
  `maven-publish`
}

group = "in.stock.me"
version = "1.0.0"

publishing {

  repositories {
    maven {
      url = URI.create("https://maven.pkg.jetbrains.space/stockme/p/main/stock-me-android")

      credentials {
        // todo commonize this logic
        Properties().apply {
          try {
            load(FileInputStream(File("${rootProject.rootDir.absolutePath}/local.properties")))
          } catch (e: FileNotFoundException) {
            put("REPO_USERNAME", System.getenv("REPO_USERNAME").toString())
            put("TOKEN", System.getenv("TOKEN").toString())
          }

          username = get("REPO_USERNAME") as String
          password = get("TOKEN") as String
        }
      }
    }
  }
  publications {
    withType<MavenPublication> {
      artifactId = "di-runtime" + artifactId.replace(project.name, "")
    }
  }
}

kotlin {
  applyDefaultHierarchyTemplate()

  linuxArm64()
  linuxX64()
  macosX64()
  macosArm64()
  iosArm64()
  iosX64()
  iosSimulatorArm64()
  jvm()

  sourceSets {
    commonMain {
      dependencies {
        implementation(libs.kotlin.inject.runtime)
      }
    }
  }

  jvmToolchain(17)
}

dependencies {
  kotlin.targets.filterIsInstance<KotlinNativeTarget>().forEach {
    add("ksp${it.name.capitalized()}", libs.kotlin.inject.compiler)
  }

  kotlin.targets.filterIsInstance<KotlinJvmTarget>().forEach {
    add("ksp${it.name.capitalized()}", libs.kotlin.inject.compiler)
  }

  kspCommonMainMetadata(libs.kotlin.inject.compiler)
}

tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinCompile<*>>().configureEach {
  if (name != "kspCommonMainKotlinMetadata") {
    dependsOn("kspCommonMainKotlinMetadata")
  }
}

tasks.named("sourcesJar") {
  dependsOn("kspCommonMainKotlinMetadata")
}
