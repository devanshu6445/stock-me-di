package `in`.stock.core.di.compiler.generators

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toAnnotationSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import `in`.stock.core.di.compiler.core.FlexibleCodeGenerator
import `in`.stock.core.di.compiler.core.Generator
import `in`.stock.core.di.compiler.core.writeTo
import `in`.stock.core.di.compiler.data.ProvidesInfo
import `in`.stock.core.di.compiler.utils.INJECT
import `in`.stock.core.di.runtime.components.Provider
import javax.inject.Inject

class ProviderGenerator @Inject constructor(
  private val codeGenerator: FlexibleCodeGenerator
) : Generator<ProvidesInfo, Unit> {
  override fun generate(data: ProvidesInfo) {
    val resolvedDepType = data.resolvedDepType

    val resolvedDependencies = data.dependencies.map { it.resolve() }

    val className = data.providerName.simpleName

    FileSpec.builder(
      resolvedDepType.declaration.packageName.asString(),
      className
    ).addType(
      TypeSpec.classBuilder(className)
        .addAnnotation(INJECT)
        .addAnnotation(data.scope.toAnnotationSpec())
        .addSuperinterface(
          Provider::class.asTypeName().parameterizedBy(
            TypeVariableName.invoke(resolvedDepType.toClassName().simpleName)
          )
        )
        .constructorBuilder(
          parametersType = resolvedDependencies,
          parametersName = data.parametersName
        )
        .binderProp(
          type = resolvedDepType,
          functionClass = data.moduleClass,
          binderFunctionName = data.functionName.getShortName(),
          dependenciesName = data.parametersName
        ).build()
    ).build().writeTo(codeGenerator)
  }

  private fun TypeSpec.Builder.constructorBuilder(
    parametersType: List<KSType>,
    parametersName: List<KSName>
  ) = apply {
    if (parametersName.isEmpty() || parametersType.isEmpty()) return@apply

    val constructorBuilder = FunSpec.constructorBuilder()

    parametersType.forEachIndexed { index, type ->
      val name = parametersName[index]
      constructorBuilder
        .addParameter(
          name = name.asString(),
          type = type.toTypeName()
        )
    }
    primaryConstructor(constructorBuilder.build())
  }

  private fun TypeSpec.Builder.binderProp(
    type: KSType,
    functionClass: KSClassDeclaration,
    binderFunctionName: String,
    dependenciesName: List<KSName>
  ) = apply {
    addProperty(
      PropertySpec.builder(
        name = "instance",
        type = type.toTypeName(),
        KModifier.OVERRIDE,
      ).delegate(
        CodeBlock.builder()
          .addStatement(
            format = """
                        lazy { %T.${binderFunctionName}(${
              buildString {
                dependenciesName.forEach {
                  append(it.asString())
                  append(" = ")
                  append(it.asString())
                }
              }
            }) }
                    """.trimIndent(),
            functionClass.toClassName(),
          ).build()
      ).build()
    )
  }
}