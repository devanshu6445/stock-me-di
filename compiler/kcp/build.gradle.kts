import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
  `kotlin-dsl`
  `maven-publish`
  alias(libs.plugins.org.jetbrains.kotlin.jvm)
  id("stock.me.di.merge-tests")
}

group = "in.stock.me"
version = "1.0.0"

gradlePlugin {
  plugins {
    register("di-gradle") {
      id = "plugin.di.compiler"
      implementationClass = "in.stock.core.di.kcp.DiGradlePlugin"
      version = project.version
    }
  }
}

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
  compileOnly(libs.kotlin.gradle.plugin)
  testImplementation(projects.compiler.core)
  testImplementation(libs.ksp.testing)
  testImplementation(libs.koTest)
  testImplementation(projects.compiler.ksp)
  testImplementation(libs.kotlin.inject.compiler)
}