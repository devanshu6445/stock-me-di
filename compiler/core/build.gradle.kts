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