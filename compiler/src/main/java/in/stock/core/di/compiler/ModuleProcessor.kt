package `in`.stock.core.di.compiler

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import `in`.stock.core.di.compiler.core.Generator
import `in`.stock.core.di.compiler.core.KspBaseProcessor
import `in`.stock.core.di.compiler.core.TypeCollector
import `in`.stock.core.di.compiler.data.*
import `in`.stock.core.di.compiler.utils.*
import `in`.stock.core.di.runtime.annotations.Component
import `in`.stock.core.di.runtime.annotations.EntryPoint
import `in`.stock.core.di.runtime.annotations.InstallIn
import `in`.stock.core.di.runtime.annotations.Module
import javax.inject.Inject

class ModuleProcessor(
  environment: SymbolProcessorEnvironment,
) : KspBaseProcessor(environment) {

  private var deferred: MutableList<KSClassDeclaration> = mutableListOf()

  @Inject
  lateinit var moduleGenerator: Generator<ModuleInfo, Unit>

  @Inject
  lateinit var componentGenerator: Generator<ComponentInfo, ComponentGeneratorResult>

  @Inject
  lateinit var moduleProviderGenerator: Generator<ModuleInfo, ModuleProviderResult>

  private val currentRoundModules = mutableListOf<Pair<ModuleInfo, ModuleProviderResult>>()

  private val allGeneratedModule = mutableListOf<Pair<ModuleInfo, ModuleProviderResult>>()

  private val components = mutableListOf<KSClassDeclaration>()

  @Inject
  lateinit var moduleProviderRegistryGenerator: Generator<List<ModuleProviderResult>, Unit>

  @Inject
  lateinit var entryPointGenerator: Generator<Sequence<KSDeclaration>, Unit>

  @Inject
  lateinit var typeCollector: TypeCollector

  override fun processSymbols(resolver: Resolver): List<KSAnnotated> {
    currentRoundModules.clear()
    val previousDeferred = deferred
    deferred = mutableListOf()

    for (element in previousDeferred + resolver.getSymbols<KSClassDeclaration>(Module::class)) {
      if (validate(element)) {
        process(element)
      } else {
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

  private fun process(element: KSClassDeclaration) {

    val scope = element.scopeAnnotation()
    val providers = mutableListOf<ProvidesInfo>()

    element.getAllFunctions().forEach { provider ->
      if (provider.hasAnnotation(Provides.packageName, Provides.simpleName)) {
        provider.returnType?.also { returnType ->
          providers.add(
            ProvidesInfo(
              functionName = provider.simpleName,
              moduleClass = element,
              scope = scope,
              dependencies = provider.parameters.map { it.type },
              dependencyType = returnType,
              parametersName = provider.parameters.map {
                it.name as KSName // Parameter name must always be non-null
              }
            )
          )
        }
      }
    }

    val moduleInfo = ModuleInfo(
      root = element,
      scope = scope,
      installInComponent = element.findAnnotation(InstallIn::class.qualifiedName.value) as KSClassDeclaration,
      providers = providers
    )

    moduleGenerator.generate(data = moduleInfo)

    val moduleProviderResult = moduleProviderGenerator.generate(moduleInfo)

    currentRoundModules.add(Pair(moduleInfo, moduleProviderResult))
  }

  private fun validate(element: KSClassDeclaration): Boolean {
    if (!element.validate()) return false

    if (element.classKind != ClassKind.OBJECT)
      logger.error("Module must be a object class", element)

    return element.hasScope()
  }

  private fun KSClassDeclaration.hasScope(): Boolean {
    return runCatching {
      scopeAnnotation()
    }.isSuccess
  }

  private fun KSClassDeclaration.scopeAnnotation() = annotations.filter {
    it.annotationType.resolve()
      .declaration.annotations.any { annotation ->
        annotation.annotationType.resolve().declaration.qualifiedName?.asString() == Scope.canonicalName
      }
  }.single()

  class Provider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
      return ModuleProcessor(
        environment = environment,
      )
    }
  }
}