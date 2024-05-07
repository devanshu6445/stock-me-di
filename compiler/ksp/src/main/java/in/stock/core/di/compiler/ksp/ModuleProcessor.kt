package `in`.stock.core.di.compiler.ksp

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.validate
import `in`.stock.core.di.compiler.core.Generator
import `in`.stock.core.di.compiler.core.KspResolver
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

  private var deferred: MutableList<KSClassDeclaration> = mutableListOf()

  @Inject
  lateinit var componentGenerator: Generator<ComponentInfo, ComponentGeneratorResult>

  private val currentRoundModules = mutableListOf<Pair<ModuleInfo, ModuleProviderResult>>() //todo change to sequence

  private val allGeneratedModule = mutableListOf<Pair<ModuleInfo, ModuleProviderResult>>()

  private val components = mutableListOf<KSClassDeclaration>()

  @Inject
  lateinit var moduleProviderRegistryGenerator: Generator<List<ModuleProviderResult>, Unit>

  @Inject
  lateinit var entryPointGenerator: Generator<Sequence<KSDeclaration>, Unit>

  @Inject
  lateinit var typeCollector: TypeCollector

  @Inject
  lateinit var processModule: ProcessModule

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

  override fun onProcessSymbols(resolver: KspResolver): List<KSAnnotated> {
    currentRoundModules.clear()
    val previousDeferred = deferred
    deferred = mutableListOf()

    for (element in previousDeferred + resolver.getSymbols<KSClassDeclaration>(Module::class)) {
      val isModuleProcessingFailed = runCatching {
        currentRoundModules.add(processModule.process(element))
      }.isFailure

      if (isModuleProcessingFailed) {
        deferred += element
      }
    }

    for (component in previousDeferred + resolver.getSymbols<KSClassDeclaration>(Component::class)) {
      if (component.validate()) {
        components.add(component)
      } else {
        deferred += component
      }
    }

    if (currentRoundModules.isEmpty()) {
      runCatching {
        components.forEach { component ->
          componentGenerator.generate(
            data = ComponentInfo(
              root = component,
              modules = allGeneratedModule.map { it.first },
              modulesProvider = allGeneratedModule.map { it.second }
            )
          )
        }
      }

      runCatching {
        moduleProviderRegistryGenerator.generate(
          data = allGeneratedModule.map { it.second }
        )
      }
    }

    allGeneratedModule.addAll(currentRoundModules)

    entryPointGenerator.generate(resolver.getSymbols<KSDeclaration>(EntryPoint::class))

    return deferred
  }

  class Provider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
      return ModuleProcessor(
        environment = environment,
      )
    }
  }
}