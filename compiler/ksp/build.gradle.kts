plugins {
  alias(libs.plugins.org.jetbrains.kotlin.jvm)
  `maven-publish`
  alias(libs.plugins.ksp)
  id("stock.me.di.merge-tests")
	id("maven.publish")
}

group = "in.bitzz"
version = "0.0.1-SNAPSHOT"

mavenPublishing {
	coordinates(
		groupId = project.group.toString(),
		artifactId = "di-compiler",
		version = project.version.toString()
	)
}

tasks.withType<Test> {
  useJUnitPlatform()
}

kotlin {
  jvmToolchain(17)
}

dependencies {
  implementation(libs.di.runtime)
  ksp(libs.dagger.compiler)
  implementation(libs.di.core)
  implementation(libs.adriankuta.tree.structure)

  testImplementation(libs.ksp.testing)
  testImplementation(libs.koTest)
  testImplementation(libs.kotlin.inject.compiler)
}