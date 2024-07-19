package `in`.stock.core.di.compiler.ksp.generators

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.plusParameter
import com.squareup.kotlinpoet.ksp.toClassName
import `in`.stock.core.di.compiler.core.FlexibleCodeGenerator
import `in`.stock.core.di.compiler.core.Generator
import `in`.stock.core.di.compiler.core.writeTo
import `in`.stock.core.di.compiler.ksp.data.ComponentGeneratorResult
import `in`.stock.core.di.compiler.ksp.data.ComponentInfo
import `in`.stock.core.di.compiler.ksp.utils.COMPONENT
import `in`.stock.core.di.compiler.ksp.utils.addConstructorProperty
import `in`.stock.core.di.compiler.ksp.utils.toAnnotationSpec
import `in`.stock.core.di.compiler.ksp.utils.toLowerName
import `in`.stock.core.di.runtime.SingletonComponent
import javax.inject.Inject
import kotlin.reflect.KClass

class ComponentGenerator @Inject constructor(
	private val codeGenerator: FlexibleCodeGenerator,
) :
	Generator<ComponentInfo, ComponentGeneratorResult> {
	override fun generate(data: ComponentInfo): ComponentGeneratorResult {
		val name = data.generatedName

		FileSpec.builder(name)
			.addType(
				TypeSpec.classBuilder(name)
					.addAnnotation(COMPONENT)
					.superclass(data.root.toClassName())
					.apply {
						(data.parentComponents + data.dependencies).map {
							CodeBlock.of(
								format = "%N",
								ParameterSpec(
									name = it.simpleName.replaceFirstChar { char -> char.lowercaseChar() },
									type = it
								)
							)
						}.forEach {
							addSuperclassConstructorParameter(it)
						}
					}
					.addSuperinterfaces(data.providersToImplement)
					.constructorBuilder(
						parentComponent = data.parentComponents,
						dependencies = data.dependencies
					)
					.addModifiers(KModifier.ABSTRACT)
					.build()
			)
			.generateCreatorFunction(
				components = data.parentComponents,
				dependencies = data.dependencies,
				componentType = data.root.toClassName(),
				generatedComponent = data.generatedName
			)
			.build().writeTo(codeGenerator)

		return ComponentGeneratorResult(name = name)
	}

	private fun FileSpec.Builder.generateCreatorFunction(
		components: List<ClassName>,
		dependencies: List<ClassName>,
		componentType: TypeName,
		generatedComponent: TypeName,
	) = apply {
		addFunction(
			FunSpec.builder(MemberName(packageName, "create"))
				.receiver(KClass::class.asTypeName().plusParameter(componentType))
				.addParameters(
					components.map {
						// adding import manually for create because CodeBlock is not able to resolve the extension function import
						addImport(packageName = it.packageName, "create")
						ParameterSpec.builder(it.simpleName.replaceFirstChar { char -> char.lowercaseChar() }, it)
							.defaultValue(
								CodeBlock.of(
									"%T${
										if (it.canonicalName == SingletonComponent::class.qualifiedName) {
											".getInstance()"
										} else {
											"::class.create()"
										}
									}",
									it,
								)
							)
							.build()
					}
				)
				.addParameters(
					dependencies.map {
						ParameterSpec.builder(
							it.simpleName.replaceFirstChar { char -> char.lowercaseChar() },
							it
						).build()
					}
				)
				.returns(componentType)
				.addStatement(
					"""
                    return %T::class.create(
                    ${
						buildString {
							(components + dependencies).map { it.toLowerName() }.forEach {
								append("$it,")
							}
						}
					}
                    )
                """.trimIndent(),
					generatedComponent
				)
				.build()
		)
	}

	private fun TypeSpec.Builder.constructorBuilder(
		parentComponent: List<ClassName>,
		dependencies: List<ClassName>
	) = apply {
		val constructorBuilder = FunSpec.constructorBuilder()

		parentComponent.forEach { component ->
			val name = component.simpleName.replaceFirstChar { it.lowercaseChar() }

			constructorBuilder.addConstructorProperty(
				typeSpec = this,
				name = name,
				type = component,
				annotations = listOf(COMPONENT.toAnnotationSpec())
			)
		}

		dependencies.forEach { dependency ->
			val name = dependency.simpleName.replaceFirstChar { it.lowercaseChar() }

			constructorBuilder.addConstructorProperty(
				typeSpec = this,
				name = name,
				type = dependency,
			)
		}

		primaryConstructor(constructorBuilder.build())
	}
}