import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.ksp)
  `maven-publish`
}

group = "in.stock.me"
version = "1.0.0-SNAPSHOT"

publishing {
  publications {
    withType<MavenPublication> {
      artifactId = "di-runtime" + artifactId.replace(project.name, "")
    }
  }
}

kotlin {
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
}

dependencies {
  kotlin.targets.filterIsInstance<KotlinNativeTarget>().forEach {
    add("ksp${it.name.capitalized()}", libs.kotlin.inject.compiler)
  }

  kotlin.targets.filterIsInstance<KotlinJvmTarget>().forEach {
    add("ksp${it.name.capitalized()}", libs.kotlin.inject.compiler)
  }
}
