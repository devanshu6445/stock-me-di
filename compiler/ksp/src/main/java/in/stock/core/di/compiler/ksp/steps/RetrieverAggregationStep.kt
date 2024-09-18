package `in`.stock.core.di.compiler.ksp.steps

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName
import `in`.stock.core.di.compiler.core.ProcessingStepValidator
import `in`.stock.core.di.compiler.core.XCodeGenerator
import `in`.stock.core.di.compiler.core.XProcessingStepVoid
import `in`.stock.core.di.compiler.core.ext.writeTo
import `in`.stock.core.di.compiler.ksp.utils.InternalPackage
import `in`.stock.core.di.compiler.ksp.utils.capitalize
import `in`.stock.core.di.runtime.annotations.Retriever
import `in`.stock.core.di.runtime.annotations.internals.Aggregated
import javax.inject.Inject

class RetrieverAggregationStep @Inject constructor(
	private val xCodeGenerator: XCodeGenerator,
	validator: AllValidator
) : XProcessingStepVoid<KSClassDeclaration, Unit>(
	validator
) {
	override fun step(node: KSClassDeclaration) {
		val aggregationClassName = ClassName(
			packageName = InternalPackage,
			node.qualifiedName?.asString().orEmpty().capitalize().replace('.', '_')
		)

		FileSpec.builder(aggregationClassName)
			.addType(
				TypeSpec.classBuilder(aggregationClassName)
					.addAnnotation(
						AnnotationSpec.builder(Aggregated::class)
							.addMember(
								CodeBlock.of(
									buildString {
										appendLine()
										appendLine("aggregationOf = %T::class,")
										appendLine("topLevelElement = %T::class")
									},
									Retriever::class.asTypeName(),
									node.toClassName()
								)
							)
							.build()
					)
					.build()
			)
			.build().writeTo(xCodeGenerator)
	}
}

class AllValidator @Inject constructor() : ProcessingStepValidator<KSClassDeclaration> {
	override fun validate(element: KSClassDeclaration): Boolean = element.validate()
}