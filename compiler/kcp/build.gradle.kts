import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
  `kotlin-dsl`
  `maven-publish`
  alias(libs.plugins.org.jetbrains.kotlin.jvm)
  id("stock.me.di.merge-tests")
	alias(libs.plugins.com.vanniktech.maven.publish)
	signing
}

group = "in.bitzz"
version = "0.0.1"

mavenPublishing {
	coordinates(
		groupId = project.group.toString(),
		artifactId = "kdi-compiler-kcp",
		version = project.version.toString()
	)
	publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
	signAllPublications()

	pom {
		name.set("KDI Compiler Plugin")
		description.set("Kotlin compiler plugin for dependency injection")

		url.set("https://github.com/devanshu6445/kdi")

		licenses {
			license {
				name.set("The Apache License, Version 2.0")
				url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
			}
		}

		developers {
			developer {
				id.set("devanshu6445")
				name.set("Devanshu Pathsariya")
			}
		}

		scm {
			connection.set("https://github.com/devanshu6445/kdi.git")
			developerConnection.set("https://github.com/devanshu6445/kdi.git")
			url.set("https://github.com/devanshu6445/kdi")
		}
	}
}

gradlePlugin {
  plugins {
    register("kdi-gradle") {
      id = "in.bitzz.kdi.compiler"
      implementationClass = "in.kdi.core.kcp.DiGradlePlugin"
      version = project.version
    }

    register("kdi-gradle-internal") {
      id = "in.bitzz.kdi.compiler.internal"
      implementationClass = "in.kdi.core.kcp.InternalDiPlugin"
      version = project.version
    }
  }
}

kotlin {
  jvmToolchain(17)
}

tasks.withType<KotlinCompilationTask<*>>().configureEach {
  compilerOptions {
    freeCompilerArgs.add("-opt-in=org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
  }
}

tasks.withType<Test>().configureEach {
  useJUnitPlatform()
}

dependencies {
  compileOnly(libs.kotlinCompilerEmbeddable)
  compileOnly(libs.kotlin.gradle.plugin)

	// For testing
  testImplementation(projects.compiler.core)
  testImplementation(libs.ksp.testing)
  testImplementation(libs.koTest)
  testImplementation(projects.compiler.ksp)
  testImplementation(libs.kotlin.inject.compiler)
}

tasks.withType<AbstractPublishToMaven>().configureEach {
	dependsOn(tasks.withType<Sign>())
}