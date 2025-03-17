package `in`.stock.core.di.compiler.ksp.utils

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asClassName
import `in`.stock.core.di.compiler.ksp.ext.getArgument
import `in`.stock.core.di.runtime.annotations.Module
import `in`.stock.core.di.runtime.annotations.internals.Aggregated
import `in`.stock.core.di.runtime.annotations.internals.ModuleProvider
import java.util.*
import kotlin.reflect.KClass

internal val primitiveTypes =
	setOf("Int", "Long", "Short", "Byte", "Char", "Float", "Double", "Boolean", "String")
val KSValueParameter.isPrimitive: Boolean
	get() = primitiveTypes.contains(type.toString()) &&
		!(type.resolve().isFunctionType || type.resolve().isSuspendFunctionType)

fun KSAnnotated.getAnnotation(clazz: KClass<*>) =
	annotations.firstOrNull { it.annotationType.resolve().declaration.qualifiedName?.asString() == clazz.qualifiedName }

fun KSAnnotated.getAnnotationNonNull(clazz: KClass<*>) =
	annotations.first { it.annotationType.resolve().declaration.qualifiedName?.asString() == clazz.qualifiedName }

@OptIn(KspExperimental::class)
fun Resolver.getAllProviders(): Sequence<KSFunctionDeclaration> {
	suspend fun SequenceScope<KSFunctionDeclaration>.visit(declarations: Sequence<KSDeclaration>) {
		declarations.forEach {
			when (it) {
				is KSClassDeclaration -> {
					if (it.hasAnnotation(Module::class)) {
						visit(it.getAllFunctions())
					}
				}

				is KSFunctionDeclaration -> {
					if (it.hasAnnotation(Provides.packageName, Provides.simpleName)) {
						yield(it)
					}
				}
			}
		}
	}

	return sequence {
		// get all the provider from the current module
		for (file in getAllFiles()) {
			visit(file.declarations)
		}

		// get all the provider from other dependent module
		visit(getDeclarationsFromPackage(InternalPackage))
	}
}

@OptIn(KspExperimental::class)
fun Resolver.getAllModuleProviders(): Sequence<KSClassDeclaration> {
	suspend fun SequenceScope<KSClassDeclaration>.visit(declarations: Sequence<KSDeclaration>) {
		declarations.forEach {
			when (it) {
				is KSClassDeclaration -> {
					if (it.hasAnnotation(ModuleProvider::class)) {
						yield(it)
					}

					if (it.hasAnnotation(Aggregated::class) && it.getArgument<KSType>(
							Aggregated::class,
							"aggregationOf"
						).declaration.qualifiedName?.asString() == Aggregated::class.qualifiedName
					) {
						yield(
							it.getArgument<KSType>(
								Aggregated::class,
								"topLevelElement"
							).declaration as KSClassDeclaration
						)
					}
				}
			}
		}
	}

	return sequence {
		// get all the provider from the current module
		for (file in getAllFiles()) {
			visit(file.declarations)
		}

		// get all the provider from other dependent module
		visit(getDeclarationsFromPackage(InternalPackage))
	}
}

fun Resolver.getSymbolsWithClassAnnotation(
	clazz: KClass<*>
) = with(clazz.asClassName()) {
	getSymbolsWithClassAnnotation(
		packageName = packageName,
		simpleName = simpleName
	)
}

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
		for (file in getAllFiles()) {
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

fun String.camelCase() = replaceFirstChar { char -> char.lowercaseChar() }