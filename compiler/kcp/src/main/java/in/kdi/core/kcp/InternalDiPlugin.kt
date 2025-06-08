package `in`.kdi.core.kcp

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

const val GroupId = "in.stock.me"
const val ArtifactId = "di-kotlin-compiler"
const val PluginVersion = "1.0.0"

const val CompilerPluginId = "stock-me-di-compiler"

open class InternalDiPlugin : KotlinCompilerPluginSupportPlugin {

	override fun apply(target: Project) {
		super.apply(target)
		target.extensions.create("di", DiCompilerExt::class.java)
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
	var enabled: Boolean = true,
	var setupWholeKsp: Boolean = true
)