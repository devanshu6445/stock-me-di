package `in`.stock.core.di.compiler.ksp.steps

import com.google.devtools.ksp.symbol.KSClassDeclaration
import `in`.stock.core.di.compiler.core.Generator
import `in`.stock.core.di.compiler.core.XProcessingStep
import `in`.stock.core.di.compiler.ksp.data.ComponentGeneratorResult
import `in`.stock.core.di.compiler.ksp.data.ComponentInfo
import `in`.stock.core.di.compiler.ksp.data.ModuleInfo
import `in`.stock.core.di.compiler.ksp.data.ModuleProviderResult
import `in`.stock.core.di.compiler.ksp.validators.ComponentValidator
import javax.inject.Inject

class ComponentProcessingStep @Inject constructor(
	private val componentGenerator: Generator<ComponentInfo, ComponentGeneratorResult>,
	private val moduleProviderRegistryGenerator: Generator<List<ModuleProviderResult>, Unit>,
	validator: ComponentValidator
) : XProcessingStep<KSClassDeclaration, ComponentGeneratorResult, ComponentProcessingStep.Params>(
	validator
) {

	override fun step(node: KSClassDeclaration, data: Params): ComponentGeneratorResult {
		val result = componentGenerator.generate(
			data = ComponentInfo(
				root = node,
				modules = data.generatedModules.map { it.first },
				modulesProvider = data.generatedModules.map { it.second }
			)
		)

		moduleProviderRegistryGenerator.generate(
			data = data.generatedModules.map { it.second }
		)

		return result
	}

	data class Params(
		val generatedModules: List<Pair<ModuleInfo, ModuleProviderResult>>
	)
}
