package `in`.stock.core.di.compiler.ksp

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asClassName
import `in`.stock.core.di.compiler.core.Generator
import `in`.stock.core.di.compiler.core.KspResolver
import `in`.stock.core.di.compiler.core.ProcessingStep
import `in`.stock.core.di.compiler.core.processor.KspBaseProcessor
import `in`.stock.core.di.compiler.ksp.data.ComponentGeneratorResult
import `in`.stock.core.di.compiler.ksp.data.ComponentInfo
import `in`.stock.core.di.compiler.ksp.data.ModuleInfo
import `in`.stock.core.di.compiler.ksp.data.ModuleProviderResult
import `in`.stock.core.di.compiler.ksp.di.DaggerCompilerComponent
import `in`.stock.core.di.compiler.ksp.di.ProcessorMapper
import `in`.stock.core.di.runtime.annotations.Component
import `in`.stock.core.di.runtime.annotations.EntryPoint
import `in`.stock.core.di.runtime.annotations.Module
import javax.inject.Inject

class ModuleProcessor(
  environment: SymbolProcessorEnvironment
) : KspBaseProcessor(environment) {

  @Inject
  lateinit var componentGenerator: Generator<ComponentInfo, ComponentGeneratorResult>

  private val currentRoundModules = mutableListOf<Pair<ModuleInfo, ModuleProviderResult>>() //todo change to sequence

  private val allGeneratedModule = mutableSetOf<Pair<ModuleInfo, ModuleProviderResult>>()

  @Inject
  lateinit var moduleProviderRegistryGenerator: Generator<List<ModuleProviderResult>, Unit>

  @Inject
  lateinit var entryPointGenerator: Generator<KSDeclaration, Unit>

  @Inject
  lateinit var typeCollector: TypeCollector

  @Inject
  lateinit var moduleProcessingStep: ProcessingStep<KSClassDeclaration, Pair<ModuleInfo, ModuleProviderResult>>

  override val annotations: List<ClassName>
    get() = listOf(
      Module::class.asClassName(),
      Component::class.asClassName(),
      EntryPoint::class.asClassName()
    )

  override fun onCreate(resolver: KspResolver) {
    ProcessorMapper(
      DaggerCompilerComponent.factory().create(
        kspLogger = logger,
        codeGenerator = codeGenerator,
        resolver = resolver,
        messenger = messenger
      ),
      this
    ).injectProcessors()
  }

  override fun processSymbol(resolver: KspResolver, symbol: KSAnnotated, annotation: ClassName): Boolean {

    val isProcessed = when (annotation.canonicalName) {
      Module::class.qualifiedName -> {
        runCatching {
          currentRoundModules.add(moduleProcessingStep.process(symbol as KSClassDeclaration))
        }.isSuccess
      }

      Component::class.qualifiedName -> {
        if (symbol as? KSClassDeclaration == null) return false
        if (symbol.validate() && currentRoundModules.isEmpty()) {
          runCatching {
            componentGenerator.generate(
              data = ComponentInfo(
                root = symbol,
                modules = allGeneratedModule.map { it.first },
                modulesProvider = allGeneratedModule.map { it.second }
              )
            )

            moduleProviderRegistryGenerator.generate(
              data = allGeneratedModule.map { it.second }
            )
          }.isSuccess // todo check if need to differ the symbol is any one is failed
        } else {
          false
        }
      }

      EntryPoint::class.qualifiedName -> {
        entryPointGenerator.generate(symbol as KSClassDeclaration)
        true
      }

      else -> false
    }

    allGeneratedModule.addAll(currentRoundModules)

    return isProcessed
  }

  override fun roundFinished() {
    currentRoundModules.clear()
  }

  class Provider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
      return ModuleProcessor(
        environment = environment,
      )
    }
  }
}