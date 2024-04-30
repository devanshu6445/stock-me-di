package `in`.stock.core.di.compiler.utils

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asClassName
import java.util.Locale
import kotlin.reflect.KClass

internal val primitiveTypes =
    setOf("Int", "Long", "Short", "Byte", "Char", "Float", "Double", "Boolean", "String")
val KSValueParameter.isPrimitive: Boolean
    get() = primitiveTypes.contains(type.toString()) &&
            !(type.resolve().isFunctionType || type.resolve().isSuspendFunctionType)

val String?.value: String
    get() = this ?: ""

fun Resolver.getSymbolsWithClassAnnotation(
    packageName: String,
    simpleName: String,
): Sequence<KSClassDeclaration> {
    suspend fun SequenceScope<KSClassDeclaration>.visit(declarations: Sequence<KSDeclaration>) {
        for (declaration in declarations) {
            if (declaration is KSClassDeclaration) {
                if (declaration.hasAnnotation(packageName, simpleName)) {
                    yield(declaration)
                }
                visit(declaration.declarations)
            }
        }
    }
    return sequence {
        for (file in getNewFiles()) {
            visit(file.declarations)
        }
    }
}

fun KSAnnotated.hasAnnotation(packageName: String, simpleName: String): Boolean {
    return annotations.any { it.hasName(packageName, simpleName) }
}

fun KSAnnotated.hasAnnotation(type: KClass<*>): Boolean {
    val annotationClassName = type.asClassName()
    return annotations.any {
        it.hasName(
            annotationClassName.packageName,
            annotationClassName.simpleName
        )
    }
}

private fun KSAnnotation.hasName(packageName: String, simpleName: String): Boolean {
    // we can skip resolving if the short name doesn't match
    if (shortName.asString() != simpleName) return false
    val declaration = annotationType.resolve().declaration
    return declaration.packageName.asString() == packageName
}

fun ClassName.toAnnotationSpec() = AnnotationSpec.builder(this).build()

fun ClassName.toLowerName() = simpleName.replaceFirstChar { it.lowercaseChar() }

fun String.capitalize() = replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }