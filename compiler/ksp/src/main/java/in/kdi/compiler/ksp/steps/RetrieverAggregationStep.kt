package `in`.kdi.compiler.ksp.steps

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName
import `in`.kdi.compiler.core.ProcessingStepValidator
import `in`.kdi.compiler.core.XCodeGenerator
import `in`.kdi.compiler.core.XProcessingStepVoid
import `in`.kdi.compiler.core.ext.writeTo
import `in`.kdi.compiler.ksp.utils.InternalPackage
import `in`.kdi.compiler.ksp.utils.capitalize
import `in`.kdi.runtime.annotations.Retriever
import `in`.kdi.runtime.annotations.internals.Aggregated
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