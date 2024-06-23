plugins {
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kmp.convention) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.di.compiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.org.jetbrains.kotlin.jvm) apply false
    alias(libs.plugins.stock.me.linter)
    base
}

val testReport by tasks.registering(TestReport::class) {
    destinationDirectory = layout.buildDirectory.map { it.asFile.resolve("reports") }
}

val copyTestResults by tasks.registering(Copy::class) {
    destinationDir = layout.buildDirectory.get().asFile.resolve("test-results")
    includeEmptyDirs = false
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

val testReportApple by tasks.registering(TestReport::class) {
    destinationDirectory = layout.buildDirectory.map { it.asFile.resolve("reports") }
}

val copyTestResultsApple by tasks.registering(Copy::class) {
    destinationDir = layout.buildDirectory.get().asFile.resolve("test-results")
    includeEmptyDirs = false
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

val check: Task by tasks.getting
val checkApple: Task by tasks.creating

check.finalizedBy(testReport, copyTestResults)
checkApple.finalizedBy(testReportApple, copyTestResultsApple)