package `in`.stock.core.di.compiler.ksp.steps

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSName
import `in`.stock.core.di.compiler.core.Generator
import `in`.stock.core.di.compiler.core.ProcessingStepValidator
import `in`.stock.core.di.compiler.core.XProcessingStepVoid
import `in`.stock.core.di.compiler.ksp.data.ModuleInfo
import `in`.stock.core.di.compiler.ksp.data.ModuleProviderResult
import `in`.stock.core.di.compiler.ksp.data.ProvidesInfo
import `in`.stock.core.di.compiler.ksp.utils.*
import `in`.stock.core.di.runtime.annotations.InstallIn
import javax.inject.Inject

class ModuleProcessingStep @Inject constructor(
  validator: ProcessingStepValidator<KSClassDeclaration>,
  private val moduleGenerator: Generator<ModuleInfo, Unit>,
  private val moduleProviderGenerator: Generator<ModuleInfo, ModuleProviderResult>,
) : XProcessingStepVoid<KSClassDeclaration, @JvmSuppressWildcards Pair<ModuleInfo, ModuleProviderResult>>(
  validator
) {

	override fun step(node: KSClassDeclaration): Pair<ModuleInfo, ModuleProviderResult> {
    val scope = node.findAnnotation(Scope.canonicalName)
    val providers = mutableListOf<ProvidesInfo>()

    node.getAllFunctions().forEach { provider ->
      if (provider.hasAnnotation(Provides.packageName, Provides.simpleName)) {
        provider.returnType?.also { returnType ->
          providers.add(
            ProvidesInfo(
              functionName = provider.simpleName,
              moduleClass = node,
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
      root = node,
      scope = scope,
      installInComponent = node.getAnnotationArgument(InstallIn::class.qualifiedName.orEmpty()) as KSClassDeclaration,
      providers = providers
    )

    moduleGenerator.generate(data = moduleInfo)

    val moduleProviderResult = moduleProviderGenerator.generate(moduleInfo)

    return Pair(moduleInfo, moduleProviderResult)
  }
}