import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
  alias(libs.plugins.org.jetbrains.kotlin.jvm)
  `maven-publish`
  id("stock.me.di.merge-tests")
}

group = "in.stock.me"
version = "1.0.0"

publishing {
  publications {
    create("Maven", MavenPublication::class.java) {
      artifactId = "di-kotlin-compiler"
      from(components["kotlin"])
    }
  }
}

kotlin {
  jvmToolchain(17)
}

tasks.withType<KotlinCompilationTask<*>>().configureEach {
  compilerOptions {
    freeCompilerArgs.add("-opt-in=org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
  }
}

tasks.withType<Test>().configureEach {
  useJUnitPlatform()
}

dependencies {
  compileOnly(libs.kotlinCompilerEmbeddable)
  testImplementation(projects.compiler.core)
  testImplementation(libs.ksp.testing)
  testImplementation(libs.koTest)
  testImplementation(projects.compiler.ksp)
  testImplementation(libs.kotlin.inject.compiler)
}