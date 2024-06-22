import `in`.stock.tools.buildSrc.kspKmp

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kmp.convention)
    alias(libs.plugins.ksp)
    `maven-publish`
}

group = "in.stock.me"

// stockMePublish {
//    group = "in.stock.me"
//    publishingName = "di-runtime"
//    version = "1.0.0"
//    isSnapshot = true
// }

publishing {
    publications {
        withType<MavenPublication> {
          artifactId = "di-runtime" + artifactId.replace(project.name, "")
            version = "1.0.0-SNAPSHOT"
        }
    }
}

targets {
    setupAndroidTarget()
    setupIosTarget()
    setupDesktopTarget()

    commonMain {
        dependencies {
            implementation(libs.kotlin.inject.runtime)
        }
    }
}

dependencies {
    kspKmp(libs.kotlin.inject.compiler)
}

android {
    namespace = "in.stock.core.di.runtime"
}