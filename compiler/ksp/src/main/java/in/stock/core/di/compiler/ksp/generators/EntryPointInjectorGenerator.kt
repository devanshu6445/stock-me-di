package `in`.stock.core.di.compiler.ksp.generators

import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toTypeName
import `in`.stock.core.di.compiler.core.Generator
import `in`.stock.core.di.compiler.core.XCodeGenerator
import `in`.stock.core.di.compiler.core.ext.writeTo
import `in`.stock.core.di.compiler.ksp.ext.getArgument
import `in`.stock.core.di.runtime.annotations.EntryPoint
import javax.inject.Inject

class EntryPointInjectorGenerator @Inject constructor(
	private val xCodeGenerator: XCodeGenerator
) : Generator<KSClassDeclaration, String> {
	override fun generate(data: KSClassDeclaration): String {
		val (superClasses, superInterfaces) = data.superTypes.partition {
			(it.resolve().declaration as KSClassDeclaration).getConstructors().count() > 0
		}

		val initializer = data.getArgument<String>(EntryPoint::class, "initializer")

		if (initializer == "constructor") {
			return ""
		}

		val injectorGeneratorFunction =
			data.getAllFunctions()
				.first { it.simpleName.asString() == initializer }

		val transformedClassName = "KDI_${data.simpleName.asString()}"
		FileSpec.builder(
			ClassName(
				packageName = data.packageName.asString(),
				transformedClassName
			)
		)
			.addType(
				TypeSpec.classBuilder(transformedClassName)
					.apply {
						superClasses.forEach {
							superclass(it.toTypeName())
						}
						addSuperinterfaces(superInterfaces.map { it.toTypeName() })

						// just a workaround for the Ir and Synthetic Resolver
						// as the synthetic resolver is adding the component property
						addProperty(
							PropertySpec.builder(
								name = "component",
								type = ClassName(packageName = data.packageName.asString(), "${data.simpleName.asString()}Component")
							).mutable().addModifiers(
								KModifier.PROTECTED,
								KModifier.LATEINIT,
								KModifier.OPEN
							).build()
						)

						addFunction(
							FunSpec.builder(injectorGeneratorFunction.simpleName.asString())
								.addModifiers(KModifier.OVERRIDE)
								.returns(injectorGeneratorFunction.returnType!!.toTypeName())
								.addCode(
									CodeBlock.of(
										"""
										this.component = ${data.simpleName.asString()}Component::class.createBoundedComponent(this as ${data.simpleName.asString()}).apply { inject(this@$transformedClassName) }
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
		return transformedClassName
	}
}