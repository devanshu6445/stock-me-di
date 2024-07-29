package `in`.stock.core.di.compiler.ksp.data

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.toClassName
import `in`.stock.core.di.compiler.ksp.utils.hasAnnotation
import `in`.stock.core.di.runtime.annotations.Component

data class ComponentInfo(
  val root: KSClassDeclaration,
  val modules: List<ModuleInfo>,
  val modulesProvider: List<ModuleProviderResult>
) {
  val generatedName by lazy {
    ClassName(root.toClassName().packageName, "Generated${root.simpleName.asString()}")
  }

  private val constructorParameters by lazy {
    root.primaryConstructor?.parameters ?: listOf()
  }

	// todo can use partition for `parentComponents` and `dependencies`
  val parentComponents by lazy {
    constructorParameters.filter { it.hasAnnotation(Component::class) }
      .map { it.type.resolve().toClassName() }
  }

  val dependencies by lazy {
    constructorParameters.filter { !it.hasAnnotation(Component::class) }
      .map { it.type.resolve().toClassName() }
  }

  val providersToImplement by lazy {
    val parentComponents = parentComponents.map { it.canonicalName }
    modulesProvider.filter {
			// check whether this Module Provider is installed/created for any of the parent components or
			// for this component(For which we are generating this component) itself
      val installIn = it.installingComponent.toClassName().canonicalName
      parentComponents.contains(installIn) || root.qualifiedName?.asString() == installIn
    }.map { it.name }
  }
}