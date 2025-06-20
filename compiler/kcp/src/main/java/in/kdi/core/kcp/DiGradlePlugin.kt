package `in`.kdi.core.kcp

import org.gradle.api.Project
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

private const val DICompilerVersion = "0.0.1-SNAPSHOT"
private const val DIRuntimeVersion = "0.0.1-SNAPSHOT"
private const val KotlinInjectVersion = "0.8.0"

class DiGradlePlugin : InternalDiPlugin() {

	override fun apply(target: Project) {
		super.apply(target)

		val ext = target.extensions.getByType<DiCompilerExt>()

		if (ext.setupWholeKsp) {
			target.dependencies {
				if (target.plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")) {
					configureKspForMultiplatform(
						dependencyNotation = "me.tatarka.inject:kotlin-inject-compiler-ksp:$KotlinInjectVersion",
						project = target
					)

					configureKspForMultiplatform(
						dependencyNotation = "in.bitzz:di-compiler:$DICompilerVersion",
						project = target
					)
				} else {
					add("ksp", "me.tatarka.inject:kotlin-inject-compiler-ksp:$KotlinInjectVersion")
					add("ksp", "in.bitzz:di-compiler:$DICompilerVersion")
				}

				add("implementation", "me.tatarka.inject:kotlin-inject-runtime:$KotlinInjectVersion")

				add("implementation", "in.bitzz:di-runtime:$DIRuntimeVersion")
			}
		}
	}

	private fun DependencyHandlerScope.configureKspForMultiplatform(dependencyNotation: Any, project: Project) {
		val kmpExt = project.extensions.getByType<KotlinMultiplatformExtension>()
		kmpExt.targets.asSequence()
			.filter {
				it.platformType != KotlinPlatformType.common
			}.forEach { target ->
				add(
					"ksp${target.targetName.capitalized()}",
					dependencyNotation,
				)
			}
	}
}