plugins {
	`maven-publish`
	alias(libs.plugins.androidLibrary)
	alias(libs.plugins.kotlin.android)
	alias(libs.plugins.ksp)
	alias(libs.plugins.di.compiler)
}

group = "in.bitzz"
version = "1.0.0"

android {
	compileSdk = 34
	namespace = "in.kdi"

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
	publications {
		create<MavenPublication>("release") {
			artifactId = "kdi-view-model"
			afterEvaluate {
				from(components["release"])
			}
		}
	}
}