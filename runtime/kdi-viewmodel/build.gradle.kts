import java.io.FileInputStream
import java.io.FileNotFoundException
import java.net.URI
import java.util.Properties
import kotlin.apply

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.di.compiler)
    `maven-publish`
}

group = "in.stock.me"
version = "1.0.0"

android {
    compileSdk = 34

    namespace = "in.kdi"

    defaultConfig {
        minSdk = 24

        testOptions.targetSdk = 34

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

repositories {
    mavenCentral()
    google()
    mavenLocal()
}

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.android)
}

kotlin {
    jvmToolchain(17)
}

publishing {
    repositories {
        maven {
            url = URI.create("https://maven.pkg.jetbrains.space/stockme/p/main/stock-me-android")

            credentials {
                // todo commonize this logic
                Properties().apply {
                    try {
                        load(FileInputStream(File("${rootProject.rootDir.absolutePath}/local.properties")))
                    } catch (e: FileNotFoundException) {
                        put("REPO_USERNAME", System.getenv("REPO_USERNAME")?.toString() ?: "")
                        put("TOKEN", System.getenv("TOKEN")?.toString() ?: "")
                    }

                    username = get("REPO_USERNAME") as String
                    password = get("TOKEN") as String
                }
            }
        }
    }
    publications {
        create<MavenPublication>("release") {
            artifactId = "kdi-view-model"
            afterEvaluate {
                from(components["release"])
            }
        }
    }
}