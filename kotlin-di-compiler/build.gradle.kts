import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    alias(libs.plugins.org.jetbrains.kotlin.jvm)
    alias(libs.plugins.stock.me.publish)
}

stockMePublish {
    group = "in.stock.me"
    publishingName = "di-kotlin-compiler"
    version = "1.0.0"
}

kotlin {
    jvmToolchain(17)
}

tasks.withType<KotlinCompilationTask<*>>().configureEach {
    compilerOptions {
        languageVersion.set(KotlinVersion.KOTLIN_2_0)
        freeCompilerArgs.add("-opt-in=org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
    }
}

dependencies {
    compileOnly(libs.kotlinCompilerEmbeddable)
}