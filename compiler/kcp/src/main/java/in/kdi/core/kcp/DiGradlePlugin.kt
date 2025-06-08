package `in`.kdi.core.kcp

import org.gradle.api.Project
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

class DiGradlePlugin : InternalDiPlugin() {

	override fun apply(target: Project) {
		super.apply(target)

		val ext = target.extensions.getByType<DiCompilerExt>()

		if (ext.setupWholeKsp) {
			target.dependencies {
				if (target.plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")) {
					configureKspForMultiplatform(
						dependencyNotation = "me.tatarka.inject:kotlin-inject-compiler-ksp:0.7.1",
						project = target
					)

					configureKspForMultiplatform(
						dependencyNotation = "in.stock.me:di-compiler:1.0.0",
						project = target
					)
				} else {
					add("ksp", "me.tatarka.inject:kotlin-inject-compiler-ksp:0.7.1")
					add("ksp", "in.stock.me:di-compiler:1.0.0")
				}

				add("implementation", "me.tatarka.inject:kotlin-inject-runtime:0.7.1")

				add("implementation", "in.stock.me:di-runtime:1.0.0")
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