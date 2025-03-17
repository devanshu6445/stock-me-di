@file:OptIn(KspExperimental::class)

import com.google.devtools.ksp.KspExperimental
import `in`.stock.core.di.plugin.addAllKspTargets

plugins {
	alias(libs.plugins.kotlinMultiplatform)
	alias(libs.plugins.ksp)
	alias(libs.plugins.com.vanniktech.maven.publish)
	id("stock.me.di.merge-tests")
	id("maven.publish")
	signing
}

mavenPublishing {
	// Define coordinates for the published artifact
	coordinates(
		groupId = "in.bitzz",
		artifactId = "di-runtime",
		version = "0.0.1-SNAPSHOT"
	)
}

kotlin {
	applyDefaultHierarchyTemplate()

	linuxArm64()
	linuxX64()
	macosX64()
	macosArm64()
	iosArm64()
	iosX64()
	iosSimulatorArm64()
	jvm()

	sourceSets {
		commonMain {
			kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")

			dependencies {
				implementation(libs.inject.kotlin.inject.runtime.kmp)
			}
		}
	}

	jvmToolchain(17)
}

dependencies {
	addAllKspTargets(
		kotlin = kotlin,
		dependencyNotation = libs.kotlin.inject.compiler
	)
}