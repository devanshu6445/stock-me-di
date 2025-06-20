plugins {
  `maven-publish`
  alias(libs.plugins.org.jetbrains.kotlin.jvm)
  alias(libs.plugins.ksp)
	alias(libs.plugins.com.vanniktech.maven.publish)
  id("stock.me.di.merge-tests")
	id("maven.publish")
}

group = "in.bitzz"
version = "0.0.1-SNAPSHOT"

mavenPublishing {
	coordinates(
		groupId = project.group.toString(),
		artifactId = "di-core",
		version = project.version.toString()
	)
}

dependencies {
  api(libs.ksp.processor.api)
  api(libs.dagger)
  api(libs.kotlin.poet)
  ksp(libs.dagger.compiler)
  implementation(libs.ksp.testing)
}

tasks.withType<Test> {
  useJUnitPlatform()
}

tasks.test {
  useJUnitPlatform()
}
kotlin {
  jvmToolchain(17)
}