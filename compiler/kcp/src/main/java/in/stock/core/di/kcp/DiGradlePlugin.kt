package `in`.stock.core.di.kcp

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.*

const val GroupId = "in.stock.me"
const val ArtifactId = "di-kotlin-compiler"
const val PluginVersion = "1.0.0"

const val CompilerPluginId = "stock-me-di-compiler"

class DiGradlePlugin : KotlinCompilerPluginSupportPlugin {

	override fun apply(target: Project) {
		super.apply(target)
		target.extensions.create("di", DiCompilerExt::class.java)

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

	override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
		val ext = kotlinCompilation.target.project.extensions.getByType<DiCompilerExt>()

		return kotlinCompilation.target.project.provider {
			listOf(
				SubpluginOption(
					key = "enabled",
					value = ext.enabled.toString()
				)
			)
		}
	}

	override fun getCompilerPluginId(): String =
		CompilerPluginId

	override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
		groupId = GroupId,
		artifactId = ArtifactId,
		version = PluginVersion
	)

	override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true
}

open class DiCompilerExt(
	var enabled: Boolean = true
)