import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

plugins {
	alias(libs.plugins.kotlinMultiplatform)
	alias(libs.plugins.ksp)
	alias(libs.plugins.di.compiler.internal)
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
			dependencies {
				implementation(libs.kotlin.inject.runtime)
			}
		}
	}

	jvmToolchain(17)
}

dependencies {
	kotlin.targets.filterIsInstance<KotlinNativeTarget>().forEach {
		add("ksp${it.name.capitalized()}", libs.kotlin.inject.compiler)
		add("ksp${it.name.capitalized()}", libs.di.compiler)
	}

	kotlin.targets.filterIsInstance<KotlinJvmTarget>().forEach {
		add("ksp${it.name.capitalized()}", libs.kotlin.inject.compiler)
		add("ksp${it.name.capitalized()}", libs.di.compiler)
	}

	kspCommonMainMetadata(libs.kotlin.inject.compiler)
	kspCommonMainMetadata(libs.di.compiler)
}

tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinCompile<*>>().configureEach {
	if (name != "kspCommonMainKotlinMetadata") {
		dependsOn("kspCommonMainKotlinMetadata")
	}
}

tasks.named("sourcesJar") {
	dependsOn("kspCommonMainKotlinMetadata")
}