package `in`.stock.core.di.compiler.ksp.ext

import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference

infix fun KSTypeReference.eqv(type: KSType) =
	resolve().declaration.qualifiedName?.asString() == type.declaration.qualifiedName?.asString()