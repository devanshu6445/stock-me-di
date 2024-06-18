package `in`.stock.core.di.compiler.ksp.utils

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType

fun KSAnnotated.getAnnotationArgument(qualifiedName: String) = (
      annotations.find { it.annotationType.resolve().declaration.qualifiedName?.asString() == qualifiedName }
    ?.arguments?.first()?.value as KSType
  ).declaration

fun KSDeclaration.findAnnotation(qualifiedName: String) = annotations.filter {
  it.annotationType.resolve()
    .declaration.annotations.any { annotation ->
      annotation.annotationType.resolve().declaration.qualifiedName?.asString() == qualifiedName
    }
}.single()