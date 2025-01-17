import `in`.stock.core.di.plugin.dependsOn

plugins {
  alias(libs.plugins.org.jetbrains.kotlin.jvm)
  alias(libs.plugins.ksp)
  application
  id("com.bennyhuo.kotlin.ir.printer") version "1.9.20-1.0.2"
  id("stock.me.di.merge-tests")
}

tasks.withType<Test> {
  useJUnitPlatform()
}
// Adding kotlin compiler plugin tp the build, doing this instead of using gradle plugin to not publish compiler
// plugin artifact o maven/mavenLocal to reflect the changes
kotlin {
  compilerOptions {
//        languageVersion.set(KotlinVersion.KOTLIN_2_0)

    freeCompilerArgs.addAll(
      listOf(
        "-Xphases-to-dump-after=ValidateIrAfterLowering",
        "-Xdump-directory=$buildDir/ir-dump/",
      )
    )
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
  ksp(projects.compiler.ksp)
  ksp(libs.kotlin.inject.compiler)

  testImplementation(libs.kotlin.inject.compiler)
  testImplementation(projects.compiler.ksp)
  testImplementation(libs.ksp.testing)
  testImplementation(libs.koTest)
  testImplementation(projects.compiler.core)

  kotlinCompilerPluginClasspath(projects.compiler.kcp)
}

application {
  mainClass.set("MainKt")
}