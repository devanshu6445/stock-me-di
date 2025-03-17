package `in`.stock.core.di.compiler.ksp.generators

import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile
import com.squareup.kotlinpoet.ksp.toAnnotationSpec
import com.squareup.kotlinpoet.ksp.toKModifier
import com.squareup.kotlinpoet.ksp.toTypeName
import `in`.stock.core.di.compiler.core.XCodeGenerator
import `in`.stock.core.di.compiler.core.XRoundEnv
import `in`.stock.core.di.compiler.core.ext.writeTo
import `in`.stock.core.di.compiler.ksp.EntryPointProcessor
import `in`.stock.core.di.compiler.ksp.ext.getArgument
import `in`.stock.core.di.runtime.annotations.EntryPoint
import `in`.stock.core.di.runtime.internal.ComponentGenerator
import javax.inject.Inject
import kotlin.reflect.KMutableProperty0

class EntryPointInjectorGenerator @Inject constructor(
	private val xCodeGenerator: XCodeGenerator
) : EntryPointProcessor {
	override fun isApplicable(xRoundEnv: XRoundEnv, node: KSDeclaration): Boolean =
		node is KSClassDeclaration

	override fun process(xRoundEnv: XRoundEnv, node: KSDeclaration) {
		if (node is KSClassDeclaration) {
			val (superClasses, superInterfaces) = node.superTypes.partition {
				(it.resolve().declaration as KSClassDeclaration).getConstructors().count() > 0
			}

			val initializer = node.getArgument<String>(EntryPoint::class, "initializer")

			if (initializer == "constructor") {
				return
			}

			val componentType = ClassName(packageName = node.packageName.asString(), "${node.simpleName.asString()}Component")

			val injectorGeneratorFunction =
				node.getAllFunctions()
					.first { it.simpleName.asString() == initializer }

			val transformedClassName = "KDI_${node.simpleName.asString()}"
			FileSpec.builder(
				ClassName(
					packageName = node.packageName.asString(),
					transformedClassName
				)
			)
				.addType(
					TypeSpec.classBuilder(transformedClassName)
						.apply {
							node.containingFile?.let { addOriginatingKSFile(it) }
							superClasses.forEach {
								superclass(it.toTypeName())
							}
							addSuperinterfaces(superInterfaces.map { it.toTypeName() })
							addSuperinterface(ComponentGenerator::class.asTypeName().parameterizedBy(componentType))

							// just a workaround for the Ir and Synthetic Resolver
							// as the synthetic resolver is adding the component property
							addProperty(
								PropertySpec.builder(
									name = "component",
									type = componentType
								).mutable().addModifiers(
									KModifier.PROTECTED,
									KModifier.LATEINIT,
									KModifier.OPEN
								).build()
							)

							addFunction(
								FunSpec.builder("generateComponent")
									.addModifiers(KModifier.OVERRIDE)
									.addCode(
										CodeBlock.of(
											"""
												return synchronized(this) {
													try {
														component!!
													} catch(e: NullPointerException) {
														component = ${node.simpleName.asString()}Component::class.createBoundedComponent(this as ${node.simpleName.asString()})
														component
													}
												}
											""".trimIndent(),
											KMutableProperty0::class.asTypeName().parameterizedBy(componentType)
										)
									)
									.returns(componentType)
									.build()
							)

							addFunction(
								FunSpec.builder(injectorGeneratorFunction.simpleName.asString())
									.apply {
										addModifiers(
											injectorGeneratorFunction.modifiers.mapNotNull { it.toKModifier() }
										)
									}
									.addParameters(
										injectorGeneratorFunction.parameters.map { param ->
											ParameterSpec.builder(name = param.name?.asString().orEmpty(), type = param.type.toTypeName())
												.addAnnotations(param.annotations.map { it.toAnnotationSpec() }.toList())
												.build()
										}
									)
									.returns(injectorGeneratorFunction.returnType!!.toTypeName())
									.addCode(
										CodeBlock.of(
											"""
										${
												if (injectorGeneratorFunction.modifiers.contains(Modifier.OVERRIDE)) {
													"super.${injectorGeneratorFunction.simpleName.asString()}(${
														injectorGeneratorFunction.parameters.map { it.name?.asString() }.toList().toString()
															.removePrefix("[").removeSuffix("]")
													})"
												} else {
													""
												}
											}
										this.generateComponent().inject(this@$transformedClassName as ${node.simpleName.asString()})
									""".trimIndent()
										)
									)
									.build()
							)
						}
						.addModifiers(KModifier.ABSTRACT)
						.build()
				)
				.build().writeTo(xCodeGenerator)
		}
	}
}