package `in`.kdi.compiler.ksp.steps

import com.google.devtools.ksp.symbol.KSClassDeclaration
import `in`.kdi.compiler.core.Generator
import `in`.kdi.compiler.core.XProcessingStep
import `in`.kdi.compiler.ksp.data.ComponentGeneratorResult
import `in`.kdi.compiler.ksp.data.ComponentInfo
import `in`.kdi.compiler.ksp.data.ModuleInfo
import `in`.kdi.compiler.ksp.data.ModuleProviderResult
import `in`.kdi.compiler.ksp.validators.ComponentValidator
import javax.inject.Inject

class ComponentProcessingStep @Inject constructor(
	private val componentGenerator: Generator<ComponentInfo, ComponentGeneratorResult>,
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

		return result
	}

	data class Params(
		val generatedModules: List<Pair<ModuleInfo, ModuleProviderResult>>
	)
}