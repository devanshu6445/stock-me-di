package `in`.stock.core.di.compiler.ksp.generators

import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toTypeName
import `in`.stock.core.di.compiler.core.FlexibleCodeGenerator
import `in`.stock.core.di.compiler.core.Generator
import `in`.stock.core.di.compiler.core.writeTo
import `in`.stock.core.di.compiler.ksp.TypeCollector
import `in`.stock.core.di.compiler.ksp.utils.addConstructorProperty
import `in`.stock.core.di.compiler.ksp.utils.capitalize
import `in`.stock.core.di.runtime.annotations.Component
import javax.inject.Inject

class EntryPointGenerator @Inject constructor(
  private val typeCollector: TypeCollector,
  private val codeGenerator: FlexibleCodeGenerator,
) : Generator<KSDeclaration, Unit> {
  override fun generate(data: KSDeclaration) {
    when (data) {
      is KSClassDeclaration -> {
        data.generateComponentForClass()
      }

      is KSFunctionDeclaration -> {
        data.generateComponentForFunction()
      }

      else -> {
        throw IllegalArgumentException("This type is not yet supported by @EntryPoint")
      }
    }
  }

  private fun KSFunctionDeclaration.generateComponentForFunction() {
    val properties = parameters.map {
      PropertySpec.builder(
        it.name?.asString().orEmpty(),
        it.type.toTypeName(),
        KModifier.ABSTRACT
      ).build()
    }

    generateComponent(
      componentName = ClassName(
        packageName.asString(),
        "${simpleName.asString().capitalize()}Component"
      ),
      properties = properties.asSequence() // todo check
    )
  }

  private fun KSClassDeclaration.generateComponentForClass() {
    val componentName = ClassName(
      packageName.asString(),
      "${
        simpleName.asString().capitalize()
      }Component"
    )

    val properties = getConstructors().flatMap { constructor ->
      constructor.parameters.map { param ->
        PropertySpec.builder(
          param.name?.asString().orEmpty() + "1",
          param.type.toTypeName(),
          KModifier.ABSTRACT
        ).build()
      }
    }
    generateComponent(
      componentName = componentName,
      properties = properties
    )
  }

  private fun KSDeclaration.generateComponent(
    componentName: ClassName,
    properties: Sequence<PropertySpec>
  ) {
    val depComponents = typeCollector.collectTypes(this).map {
      ClassName(it.packageName.asString(), it.simpleName.asString())
    }

    FileSpec.builder(componentName).addType(
      TypeSpec.classBuilder(componentName).addModifiers(KModifier.ABSTRACT)
        .addAnnotation(Component::class).constructorBuilder(depComponents)
        .addProperties(properties.asIterable()).build()
    ).build().writeTo(codeGenerator)
  }

  private fun TypeSpec.Builder.constructorBuilder(
    parentComponent: Sequence<ClassName>,
  ) = apply {
    if (parentComponent.count() == 0) return@apply

    val constructorBuilder = FunSpec.constructorBuilder()

    parentComponent.forEach { component ->

      // todo some suffix to make the name different than type name
      val name = component.simpleName.replaceFirstChar { it.lowercaseChar() } + "1"

      constructorBuilder.addConstructorProperty(
        typeSpec = this,
        name = name,
        type = component,
        annotations = listOf(
          AnnotationSpec.builder(Component::class).build()
        )
      )
    }

    primaryConstructor(constructorBuilder.build())
  }
}