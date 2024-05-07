package `in`.stock.core.di.compiler.utils

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSType

fun KSAnnotated.findAnnotation(qualifiedName: String) =
  (annotations.find { it.annotationType.resolve().declaration.qualifiedName?.asString() == qualifiedName }
    ?.arguments?.first()?.value as KSType).declaration