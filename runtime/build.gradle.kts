import `in`.kdi.plugin.addAllKspTargets

plugins {
	`maven-publish`
	alias(libs.plugins.kotlinMultiplatform)
	alias(libs.plugins.ksp)
	alias(libs.plugins.di.compiler.internal)
	id("stock.me.di.merge-tests")
	id("maven.publish")
}

group = "in.bitzz"
version = "0.0.1-SNAPSHOT"

mavenPublishing {
	// Define coordinates for the published artifact
	coordinates(
		groupId = project.group.toString(),
		artifactId = "di-runtime",
		version = project.version.toString()
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

	addAllKspTargets(
		kotlin = kotlin,
		dependencyNotation = libs.di.compiler,
	)
}