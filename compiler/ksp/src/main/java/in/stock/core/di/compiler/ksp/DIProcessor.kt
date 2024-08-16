package `in`.stock.core.di.compiler.ksp

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import `in`.stock.core.di.compiler.core.XProcessingStepVoid
import `in`.stock.core.di.compiler.core.XRoundEnv
import `in`.stock.core.di.compiler.core.exceptions.ValidationException
import `in`.stock.core.di.compiler.core.ksp.KspBaseProcessor
import `in`.stock.core.di.compiler.ksp.data.ModuleInfo
import `in`.stock.core.di.compiler.ksp.data.ModuleProviderResult
import `in`.stock.core.di.compiler.ksp.di.DaggerCompilerComponent
import `in`.stock.core.di.compiler.ksp.di.ProcessorMapper
import `in`.stock.core.di.compiler.ksp.steps.ComponentProcessingStep
import `in`.stock.core.di.compiler.ksp.steps.RetrieverAggregationStep
import `in`.stock.core.di.compiler.ksp.utils.getSymbolsWithClassAnnotation
import `in`.stock.core.di.runtime.annotations.Component
import `in`.stock.core.di.runtime.annotations.EntryPoint
import `in`.stock.core.di.runtime.annotations.Module
import `in`.stock.core.di.runtime.annotations.Retriever
import javax.inject.Inject

class DIProcessor(
	environment: SymbolProcessorEnvironment
) : KspBaseProcessor(environment) {

	private val currentRoundModules = mutableListOf<Pair<ModuleInfo, ModuleProviderResult>>() // todo change to sequence

	private val allGeneratedModule = mutableSetOf<Pair<ModuleInfo, ModuleProviderResult>>()

	@Inject
	lateinit var entryPointGenerator: XProcessingStepVoid<KSDeclaration, Unit>

	@Inject
	lateinit var moduleProcessingStep: XProcessingStepVoid<KSClassDeclaration, Pair<ModuleInfo, ModuleProviderResult>>

	@Inject
	lateinit var componentProcessingStep: ComponentProcessingStep

	@Inject
	lateinit var aggregationStep: RetrieverAggregationStep

	override val annotations: List<String>
		get() = listOf(
			Module::class.qualifiedName.orEmpty(),
			Component::class.qualifiedName.orEmpty(),
			EntryPoint::class.qualifiedName.orEmpty()
		)

	override fun preRound(xRoundEnv: XRoundEnv) {
		ProcessorMapper(
			DaggerCompilerComponent.factory().create(
				xRoundEnv = xRoundEnv
			),
			this
		).injectProcessors()
	}

	override fun processSymbol(xRoundEnv: XRoundEnv, symbol: KSAnnotated, annotationFullName: String): Boolean {
		val isProcessed = when (annotationFullName) {
			Module::class.qualifiedName -> {
				runCatching {
					currentRoundModules.add(moduleProcessingStep.process(symbol as KSClassDeclaration))
				}.isSuccess
			}

			Component::class.qualifiedName -> {
				if (symbol as? KSClassDeclaration == null) return false

				try {
					componentProcessingStep.process(
						node = symbol,
						data = ComponentProcessingStep.Params(
							generatedModules = allGeneratedModule.toList()
						)
					)
					true
				} catch (e: ValidationException) {
					xEnv.messenger.error(e.message.toString(), symbol)
					false
				} catch (e: FileAlreadyExistsException) {
					true
				} catch (e: Exception) {
					throw e
				}
			}

			EntryPoint::class.qualifiedName -> {
				if (currentRoundModules.isEmpty()) {
					entryPointGenerator.process(symbol as KSDeclaration)
					true
				} else {
					false
				}
			}

			else -> {
				false
			}
		}

		allGeneratedModule.addAll(currentRoundModules)

		return isProcessed
	}

	override fun postRound(xRoundEnv: XRoundEnv) {
		currentRoundModules.clear()
	}

	override fun finish() {
		super.finish()
		xEnv.resolver
			.getSymbolsWithClassAnnotation(Retriever::class)
			.forEach(aggregationStep::process)
	}

	class Provider : SymbolProcessorProvider {
		override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
			return DIProcessor(
				environment = environment,
			)
		}
	}
}