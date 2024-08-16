package `in`.stock.core.di.compiler.ksp.ext

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType
import `in`.stock.core.di.compiler.core.XResolver
import `in`.stock.core.di.compiler.ksp.utils.InternalPackage
import `in`.stock.core.di.compiler.ksp.utils.hasAnnotation
import `in`.stock.core.di.runtime.annotations.Retriever
import `in`.stock.core.di.runtime.annotations.internals.Aggregated

@OptIn(KspExperimental::class)
fun XResolver.getAllRetrievers() = sequence {
	suspend fun SequenceScope<KSClassDeclaration>.visitNode(node: KSDeclaration) {
		when (node) {
			is KSClassDeclaration -> {
				if (node.hasAnnotation(Retriever::class)) {
					yield(node)
					return
				}

				if (node.hasAnnotation(Aggregated::class)) {
					yield(
						node.getArgument<KSType>(Aggregated::class, "topLevelClass")
							.declaration as KSClassDeclaration
					)
				}

				node.declarations.forEach {
					visitNode(it)
				}
			}
		}
	}
	for (file in getAllFiles()) {
		file.declarations.forEach {
			visitNode(it)
		}
	}

	for (declaration in getDeclarationsFromPackage(InternalPackage)) {
		visitNode(declaration)
	}
}