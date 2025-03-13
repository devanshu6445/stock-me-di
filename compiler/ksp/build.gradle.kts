plugins {
  alias(libs.plugins.org.jetbrains.kotlin.jvm)
  `maven-publish`
  alias(libs.plugins.ksp)
  id("stock.me.di.merge-tests")
}

group = "in.stock.me"
version = "1.0.0"

publishing {
  publications {
    create<MavenPublication>("maven") {
      artifactId = "di-compiler"
      from(components["kotlin"])
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
  implementation("in.stock.me:di-core:${project.version}")
  implementation(libs.adriankuta.tree.structure)

  testImplementation(libs.ksp.testing)
  testImplementation(libs.koTest)
  testImplementation(libs.kotlin.inject.compiler)
}