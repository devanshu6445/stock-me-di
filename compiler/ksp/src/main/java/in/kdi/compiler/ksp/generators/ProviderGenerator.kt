package `in`.kdi.compiler.ksp.generators

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile
import com.squareup.kotlinpoet.ksp.toAnnotationSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import `in`.kdi.compiler.core.Generator
import `in`.kdi.compiler.core.XCodeGenerator
import `in`.kdi.compiler.core.ext.writeTo
import `in`.kdi.compiler.ksp.data.ProvidesInfo
import `in`.kdi.compiler.ksp.utils.INJECT
import `in`.kdi.runtime.annotations.internals.GeneratedDepProvider
import `in`.kdi.runtime.components.Provider
import javax.inject.Inject

class ProviderGenerator @Inject constructor(
	private val xCodeGenerator: XCodeGenerator
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
				.apply {
					resolvedDepType.declaration.containingFile?.let { addOriginatingKSFile(it) }
				}
				.addAnnotation(
					annotationSpec = AnnotationSpec.builder(GeneratedDepProvider::class.asClassName())
						.addMember(
							CodeBlock.of(
								"%L = %T::class",
								"clazz",
								resolvedDepType.toClassName()
							)
						)
						.build()
				)
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
				.addFunction(
					createProviderGetter(
						type = resolvedDepType,
						functionClass = data.moduleClass,
						binderFunctionName = data.functionName.getShortName(),
						dependenciesName = data.parametersName
					)
				)
				.binderProp(
					type = resolvedDepType,
					functionClass = data.moduleClass,
				).build()
		).build().writeTo(xCodeGenerator)
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

				addProperty(
					PropertySpec.builder(
						name = name.asString(),
						type = type.toTypeName()
					)
						.initializer(name.asString())
						.build()
				)
		}
		primaryConstructor(constructorBuilder.build())
	}

	private fun TypeSpec.Builder.binderProp(
		type: KSType,
		functionClass: KSClassDeclaration,
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
                        lazy { get() }
                    """.trimIndent(),
						functionClass.toClassName(),
					).build()
			).build()
		)
	}

	private fun createProviderGetter(
		type: KSType,
		functionClass: KSClassDeclaration,
		binderFunctionName: String,
		dependenciesName: List<KSName>
	): FunSpec {
		return FunSpec.builder(name = "get")
			.returns(type.toTypeName())
			.addModifiers(
				KModifier.OVERRIDE
			)
			.addStatement(
				"""
					return %T.$binderFunctionName(${
					buildString {
						dependenciesName.forEach {
							append(it.asString())
							append(" = ")
							append(it.asString())
						}
					}
				})
				""".trimIndent(),
				functionClass.toClassName()
			)
			.build()
	}
}