import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
  alias(libs.plugins.org.jetbrains.kotlin.jvm)
  `maven-publish`
  id("stock.me.di.merge-tests")
}

group = "in.stock.me"
version = "1.0.0"

// stockMePublish {
//    group = "in.stock.me"
//    publishingName = "di-kotlin-compiler"
//    version = "1.0.0"
// }

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

dependencies {
  compileOnly(libs.kotlinCompilerEmbeddable)
}