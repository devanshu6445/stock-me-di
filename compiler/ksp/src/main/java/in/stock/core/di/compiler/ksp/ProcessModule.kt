package `in`.stock.core.di.compiler.ksp

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSName
import `in`.stock.core.di.compiler.core.Generator
import `in`.stock.core.di.compiler.ksp.data.ModuleInfo
import `in`.stock.core.di.compiler.ksp.data.ModuleProviderResult
import `in`.stock.core.di.compiler.ksp.data.ProvidesInfo
import `in`.stock.core.di.compiler.ksp.utils.*
import `in`.stock.core.di.runtime.annotations.InstallIn
import javax.inject.Inject

interface ProcessModule {
  fun process(declaration: KSClassDeclaration): Pair<ModuleInfo, ModuleProviderResult>
}

class ProcessModuleImpl @Inject constructor(
  private val moduleValidator: ModuleValidator,
  private val moduleGenerator: Generator<ModuleInfo, Unit>,
  private val moduleProviderGenerator: Generator<ModuleInfo, ModuleProviderResult>
) : ProcessModule {

  override fun process(declaration: KSClassDeclaration): Pair<ModuleInfo, ModuleProviderResult> {
    if (moduleValidator.validate(declaration)) {
      return processModule(declaration)
    } else {
      throw IllegalStateException("Module couldn't be validated")
    }
  }

  private fun processModule(element: KSClassDeclaration): Pair<ModuleInfo, ModuleProviderResult> {
    val scope = element.findAnnotation(Scope.canonicalName)
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
      installInComponent = element.getAnnotationArgument(InstallIn::class.qualifiedName.orEmpty()) as KSClassDeclaration,
      providers = providers
    )

    moduleGenerator.generate(data = moduleInfo)

    val moduleProviderResult = moduleProviderGenerator.generate(moduleInfo)

    return Pair(moduleInfo, moduleProviderResult)
  }
}