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
}

dependencies {
  kotlin.targets.all {
    add("ksp${this.name}", libs.kotlin.inject.compiler)
  }
}
