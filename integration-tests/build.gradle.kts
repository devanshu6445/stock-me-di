import com.android.build.gradle.internal.tasks.factory.dependsOn
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
  alias(libs.plugins.org.jetbrains.kotlin.jvm)
  alias(libs.plugins.ksp)
  application
  id("com.bennyhuo.kotlin.ir.printer") version "1.9.20-1.0.2"
}

tasks.withType<Test> {
  useJUnitPlatform()
}
// Adding kotlin compiler plugin tp the build, doing this instead of using gradle plugin to not publish compiler
// plugin artifact o maven/mavenLocal to reflect the changes
kotlin {
  compilerOptions {
//        languageVersion.set(KotlinVersion.KOTLIN_2_0)
    freeCompilerArgs.add("-Xplugin=${project(":kotlin-di-compiler").projectDir}/build/libs/kotlin-di-compiler-1.0.0.jar")
  }
}

// always run a clean build, because after applying compiler plugin
// ksp generated classes are not resolved todo need to resolve the issue
afterEvaluate {
  tasks.named("kspKotlin").dependsOn("clean")
}

dependencies {
  implementation(projects.runtime)
  implementation(libs.kotlin.inject.runtime)
  ksp(projects.compiler)
  ksp(libs.kotlin.inject.compiler)

  testImplementation(libs.kotlin.inject.compiler)
  testImplementation(projects.compiler)
  testImplementation(libs.ksp.testing)
  testImplementation(libs.koTest)

  // to depend this project onto kotlin-di-compiler so that jar is generated every time this project is compiled
  implementation(projects.kotlinDiCompiler)
}

application {
  mainClass.set("MainKt")
}