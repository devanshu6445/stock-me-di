import `in`.kdi.core.di.plugin.addAllKspTargets
import kotlin.math.sign

plugins {
	`maven-publish`
	alias(libs.plugins.kotlinMultiplatform)
	alias(libs.plugins.ksp)
	alias(libs.plugins.com.vanniktech.maven.publish)
	id("stock.me.di.merge-tests")
	signing
}

mavenPublishing {
	// Define coordinates for the published artifact
	coordinates(
		groupId = "in.bitzz",
		artifactId = "di-runtime",
		version = "0.0.1-SNAPSHOT"
	)
	signAllPublications()
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