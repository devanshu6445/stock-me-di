package `in`.stock.core.di.compiler.ksp.steps

import com.google.devtools.ksp.symbol.KSClassDeclaration
import `in`.stock.core.di.compiler.core.Generator
import `in`.stock.core.di.compiler.core.ProcessingStepValidator
import `in`.stock.core.di.compiler.core.XProcessingStepVoid
import `in`.stock.core.di.compiler.ksp.data.ModuleInfo
import `in`.stock.core.di.compiler.ksp.data.ModuleProviderResult
import `in`.stock.core.di.compiler.ksp.data.ProvidesInfo
import `in`.stock.core.di.compiler.ksp.data.asModule
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