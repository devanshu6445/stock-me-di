package `in`.stock.core.di.compiler.ksp.generators

import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import `in`.stock.core.di.compiler.core.Generator
import `in`.stock.core.di.compiler.core.XCodeGenerator
import `in`.stock.core.di.compiler.core.XResolver
import `in`.stock.core.di.compiler.core.ext.writeTo
import `in`.stock.core.di.compiler.ksp.TypeCollector
import `in`.stock.core.di.compiler.ksp.ext.getAllRetrievers
import `in`.stock.core.di.compiler.ksp.ext.getArgument
import `in`.stock.core.di.compiler.ksp.utils.*
import `in`.stock.core.di.compiler.ksp.validators.ParentComponentProperty
import `in`.stock.core.di.runtime.annotations.AssociatedWith
import `in`.stock.core.di.runtime.annotations.Component
import `in`.stock.core.di.runtime.annotations.EntryPoint
import `in`.stock.core.di.runtime.annotations.Retriever
import `in`.stock.core.di.runtime.internal.GeneratedComponent
import javax.inject.Inject

class EntryPointComponentGenerator @Inject constructor(
	private val typeCollector: TypeCollector,
	private val xCodeGenerator: XCodeGenerator,
	private val xResolver: XResolver,
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
			PropertySpec.Companion.builder(
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

		// only provide this class as argument if the injection is not through the constructor
		// and this class doesn't have an primary constructor
		// todo can remove the check of constructor injection as the dependency can be provided via secondary constructor
		//  if primary constructor doesn't exist
		val canProvideThisClassAsArgument =
			annotations.first {
				it.annotationType.resolve().declaration.qualifiedName?.asString() == EntryPoint::class.qualifiedName
			}.arguments.first {
				it.name?.asString() == "initializer"
			}.value != "constructor" && (primaryConstructor == null || primaryConstructor?.origin == Origin.SYNTHETIC)

		generateComponent(
			componentName = componentName,
			properties = extractLazyStrippedProperties(),
			arguments = if (canProvideThisClassAsArgument) listOf(this.toClassName()) else listOf()
		)
	}

	private fun KSDeclaration.generateComponent(
		componentName: ClassName,
		properties: Sequence<PropertySpec>,
		arguments: List<ClassName> = emptyList()
	) {

		val parentComponent = getArgument<KSType>(EntryPoint::class, ParentComponentProperty)
		val requiredData = typeCollector.findRequiredComponents(this)
		val entryPointSpecificProviders = typeCollector.findModuleProvidersByModule(requiredData.second)
		val entryPointRetrievers = xResolver.getAllRetrievers().filter {
			it.getArgument<KSType>(Retriever::class, "component").declaration
				.qualifiedName?.asString() == parentComponent.declaration.qualifiedName?.asString()
		}

		val depComponents = requiredData.first
			.map { ClassName(it.packageName.asString(), it.simpleName.asString()) }

		val entryPointScope = xResolver.getSymbolsWithClassAnnotation(
			Scope.packageName,
			Scope.simpleName
		)
			.firstOrNull {
				it.hasAnnotation(AssociatedWith::class) &&
					(it.getAnnotationNonNull(AssociatedWith::class).arguments.first().value as KSType)
						.declaration.qualifiedName?.asString() == this.qualifiedName?.asString()
			}

		FileSpec.builder(componentName).addType(
			TypeSpec.classBuilder(componentName).addModifiers(KModifier.ABSTRACT)
				.apply {
					this@generateComponent.containingFile?.let { addOriginatingKSFile(it) }
					addSuperinterface(GeneratedComponent::class)
					addSuperinterfaces(entryPointRetrievers.map { it.toClassName() }.toList())
				}
				.addAnnotation(Component::class)
				.apply {
					if (entryPointScope != null) {
						addAnnotation(
							AnnotationSpec.Companion.builder((entryPointScope).toClassName())
								.build()
						)
					}

					if (this@generateComponent is KSClassDeclaration &&
						this@generateComponent.primaryConstructor?.origin == Origin.SYNTHETIC
					) {
						addFunction(
							FunSpec.builder("inject")
								.addParameter(
									ParameterSpec(name = "clazz", this@generateComponent.toClassName())
								)
								.addCode(
									CodeBlock.of(
										buildString {
											val propertyToType = extractProperties()

											propertyToType.forEach { ksAnnotated ->
												when (ksAnnotated) {
													is KSValueParameter -> {
														val resolvedProp = ksAnnotated.type.resolve()
														if (resolvedProp.declaration.qualifiedName?.asString() == LazyName) {
															val correspondingProperty =
																properties.first { it.type == resolvedProp.arguments.first().toTypeName() }
															appendLine("clazz.${ksAnnotated.name?.asString()} = lazy { ${correspondingProperty.name} }")
														} else {
															val correspondingProperty = properties.first { it.type == ksAnnotated.type.toTypeName() }
															appendLine("clazz.${ksAnnotated.name?.asString()} = ${correspondingProperty.name}")
														}
													}

													is KSPropertyDeclaration -> {
														val resolvedProp = ksAnnotated.type.resolve()
														if (resolvedProp.declaration.qualifiedName?.asString() == LazyName) {
															val correspondingProperty =
																properties.first { it.type == resolvedProp.arguments.first().toTypeName() }
															appendLine("clazz.${ksAnnotated.simpleName.asString()} = lazy { ${correspondingProperty.name} }")
														} else {
															val correspondingProperty = properties.first { it.type == ksAnnotated.type.toTypeName() }
															appendLine("clazz.${ksAnnotated.simpleName.asString()} = ${correspondingProperty.name}")
														}
													}
												}
											}
										}
									)
								)
								.build()
						)
					}
				}
				.addSuperinterfaces(entryPointSpecificProviders.map { it.toClassName() }.toList())
				.constructorBuilder(depComponents, arguments)
				.addProperties(properties.asIterable()).build()
		).build().writeTo(xCodeGenerator)
	}

	private fun TypeSpec.Builder.constructorBuilder(
		parentComponent: Sequence<ClassName>,
		arguments: List<ClassName>
	) = apply {
		val constructorBuilder = FunSpec.constructorBuilder()

		parentComponent.forEach { component ->

			// todo some suffix to make the name different than type name
			// workaround for conflicting declaration when inheriting the class
			// can overcome by adding override to the child class params
			val name = component.simpleName.replaceFirstChar { it.lowercaseChar() } + "1"

			constructorBuilder.addConstructorProperty(
				typeSpec = this,
				name = name,
				type = component,
				annotations = listOf(
					AnnotationSpec.builder(Component::class).build(),
					AnnotationSpec.builder(Provides)
						.useSiteTarget(AnnotationSpec.UseSiteTarget.GET)
						.build()
				)
			)
		}

		arguments.forEach { arg ->
			constructorBuilder.addConstructorProperty(
				typeSpec = this,
				name = arg.simpleName.replaceFirstChar { char -> char.lowercaseChar() } + "1",
				type = arg,
				annotations = listOf(
					AnnotationSpec.builder(Provides)
						.useSiteTarget(AnnotationSpec.UseSiteTarget.GET)
						.build()
				)
			)
		}

		primaryConstructor(constructorBuilder.build())
	}
}

private data class Property(
	val name: String,
	val type: KSTypeReference,
)

private fun KSClassDeclaration.extractProperties() = sequence {
	for (constructor in getConstructors()) {
		for (parameter in constructor.parameters) {
			yield(
				parameter
			)
		}
	}

	for (property in getAllProperties()) {
		// only yield properties which needs to be injected
		if (!property.hasAnnotation(`in`.stock.core.di.runtime.annotations.Inject::class)) {
			continue
		}

		yield(property)
	}
}

private fun KSClassDeclaration.extractLazyStrippedProperties() = sequence {
	for (constructor in getConstructors()) {
		for (parameter in constructor.parameters) {
			yield(
				Property(
					name = parameter.name?.asString() ?: return@sequence,
					type = parameter.type
				)
			)
		}
	}

	for (property in getAllProperties()) {
		// only yield properties which needs to be injected
		if (!property.hasAnnotation(`in`.stock.core.di.runtime.annotations.Inject::class)) {
			continue
		}

		yield(
			Property(
				name = property.simpleName.asString(),
				type = if (property.type.resolve().declaration.qualifiedName?.asString() == LazyName) {
					property.type.resolve().arguments.first().type!!
				} else {
					property.type
				}
			)
		)
	}
}.distinctBy {
	it.type.resolve().declaration.qualifiedName?.asString()
}.map {
	PropertySpec.builder(
		name = it.name,
		type = it.type.toTypeName(),
		KModifier.ABSTRACT
	).build()
}