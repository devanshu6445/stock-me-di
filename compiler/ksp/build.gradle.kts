plugins {
  alias(libs.plugins.org.jetbrains.kotlin.jvm)
  alias(libs.plugins.stock.me.publish)
  alias(libs.plugins.ksp)
}

tasks.withType<Test> {
  useJUnitPlatform()
}

stockMePublish {
  group = "in.stock.me"
  publishingName = "di-compiler"
  version = "1.0.0"
  isSnapshot = true
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