package `in`.kdi.compiler.ksp.steps

import com.google.devtools.ksp.symbol.KSClassDeclaration
import `in`.kdi.compiler.core.Generator
import `in`.kdi.compiler.core.ProcessingStepValidator
import `in`.kdi.compiler.core.XProcessingStepVoid
import `in`.kdi.compiler.ksp.data.ModuleInfo
import `in`.kdi.compiler.ksp.data.ModuleProviderResult
import `in`.kdi.compiler.ksp.data.ProvidesInfo
import `in`.kdi.compiler.ksp.data.asModule
import javax.inject.Inject

class ModuleProcessingStep @Inject constructor(
	validator: ProcessingStepValidator<KSClassDeclaration>,
	private val providerGenerator: Generator<ProvidesInfo, Unit>,
	private val moduleProviderGenerator: Generator<ModuleInfo, ModuleProviderResult>,
) : XProcessingStepVoid<KSClassDeclaration, @JvmSuppressWildcards Pair<ModuleInfo, ModuleProviderResult>>(
	validator
) {

	override fun step(node: KSClassDeclaration): Pair<ModuleInfo, ModuleProviderResult> {
		val module = node.asModule()

		for (provider in module.providers.distinctBy { it.resolvedDepType }) {
			if (!provider.isCollectedIntoMap) {
				providerGenerator.generate(
					data = provider
				)
			}
		}

		val moduleProviderResult = moduleProviderGenerator.generate(module)

		return Pair(module, moduleProviderResult)
	}
}