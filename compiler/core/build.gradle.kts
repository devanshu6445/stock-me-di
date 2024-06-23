plugins {
  alias(libs.plugins.org.jetbrains.kotlin.jvm)
  alias(libs.plugins.ksp)
  id("stock.me.di.merge-tests")
}

group = "in.stock.me"
version = "1.0.0-SNAPSHOT"

dependencies {
  api(libs.ksp.processor.api)
  api(libs.dagger)
  api(libs.kotlin.poet)
  ksp(libs.dagger.compiler)
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