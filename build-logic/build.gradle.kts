plugins {
  `kotlin-dsl`
}

group = "in.stock.me"
version = "1.0.0"

repositories {
  mavenCentral()
}

dependencies {
  implementation(libs.kotlin.gradle.plugin)
	implementation(libs.gradle.maven.publish.plugin)

  implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}