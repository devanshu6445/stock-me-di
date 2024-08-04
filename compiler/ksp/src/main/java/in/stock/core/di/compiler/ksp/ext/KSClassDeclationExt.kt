package `in`.stock.core.di.compiler.ksp.ext

import com.google.devtools.ksp.isConstructor
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration

fun KSClassDeclaration.getAllFunctionExceptPrimitive(): Sequence<KSFunctionDeclaration> = getAllFunctions()
	.filterNot {
		it.isConstructor() || when (it.simpleName.asString()) {
			"equals", "hashCode", "toString" -> true
			else -> false
		}
	}
