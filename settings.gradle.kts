import java.net.URI

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {

	repositories {
		maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
		maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
		google()
		mavenCentral()
		mavenLocal()
		gradlePluginPortal()
	}

	includeBuild("build-logic")
}
plugins {
	id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
dependencyResolutionManagement {
	// Do RCA and find alternative and elegant approach
	repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
	repositories {
		maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
		google()
		mavenCentral()
		mavenLocal()
		maven { url = URI.create("https://oss.sonatype.org/content/repositories/snapshots") }
	}
}

include(":runtime")
include(":compiler")
include(":idea-plugin")
include("compiler:core")
findProject(":compiler:core")?.name = "core"
include("compiler:ksp")
findProject(":compiler:ksp")?.name = "ksp"
include("compiler:kcp")
findProject(":compiler:kcp")?.name = "kcp"
include("compiler:integration-tests")
include("runtime:kdi-viewmodel")
findProject(":runtime:kdi-viewmodel")?.name = "kdi-viewmodel"
