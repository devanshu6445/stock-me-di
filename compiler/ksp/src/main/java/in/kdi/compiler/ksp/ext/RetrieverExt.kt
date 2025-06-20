package `in`.kdi.compiler.ksp.ext

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType
import `in`.kdi.compiler.core.XResolver
import `in`.kdi.compiler.ksp.utils.InternalPackage
import `in`.kdi.compiler.ksp.utils.hasAnnotation
import `in`.kdi.runtime.annotations.Retriever
import `in`.kdi.runtime.annotations.internals.Aggregated

@OptIn(KspExperimental::class)
fun XResolver.getAllRetrievers() = sequence {
	suspend fun SequenceScope<KSClassDeclaration>.visitNode(node: KSDeclaration) {
		when (node) {
			is KSClassDeclaration -> {
				if (node.hasAnnotation(Retriever::class)) {
					yield(node)
					return
				}

				if (node.hasAnnotation(Aggregated::class) && node.getArgument<KSType>(
						Aggregated::class,
						"aggregationOf"
					).declaration.qualifiedName?.asString() == Retriever::class.qualifiedName
				) {
					yield(
						node.getArgument<KSType>(Aggregated::class, "topLevelElement")
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