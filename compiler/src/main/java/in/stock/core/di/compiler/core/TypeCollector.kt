package `in`.stock.core.di.compiler.core

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import `in`.stock.core.di.compiler.core.exceptions.ClassConstructException
import `in`.stock.core.di.compiler.utils.INJECT
import `in`.stock.core.di.compiler.utils.InternalPackage
import `in`.stock.core.di.compiler.utils.Provides
import `in`.stock.core.di.compiler.utils.Scope
import `in`.stock.core.di.compiler.utils.findAnnotation
import `in`.stock.core.di.compiler.utils.hasAnnotation
import `in`.stock.core.di.compiler.utils.value
import `in`.stock.core.di.runtime.annotations.AssociatedWith
import `in`.stock.core.di.runtime.annotations.InstallIn
import `in`.stock.core.di.runtime.annotations.Module
import javax.inject.Inject

class TypeCollector @Inject constructor(
    private val resolver: KspResolver,
    private val kspLogger: KSPLogger
) {

    private val typeToComponentMap = hashMapOf<String, List<KSClassDeclaration>>()

    private val allProvidersMap by lazy {
        resolver.getAllProviders().associate {
            it.returnType?.resolve()?.declaration?.qualifiedName?.asString() to it.parentDeclaration
        }
    }

    fun collectTypes(type: KSDeclaration): Sequence<KSClassDeclaration> {
        return when (type) {
            is KSClassDeclaration, is KSFunctionDeclaration -> {
                val components = type.getComponents()
                    .distinctBy { it.qualifiedName?.asString() }
                typeToComponentMap[type.qualifiedName?.asString().value] =
                    components.toList() // todo check
                components
            }

            else -> {
                TODO()
            }
        }
    }

    private fun KSDeclaration.getDependencies(): Sequence<KSDeclaration> {

        val parameters = when (this) {
            is KSClassDeclaration -> getConstructors().flatMap { it.parameters }
            is KSFunctionDeclaration -> sequenceOf(*parameters.toTypedArray())
            else -> {
                kspLogger.error("This type is not supported by @EntryPoint", this)
                throw IllegalArgumentException("This type is not supported by @EntryPoint")
            }
        }

        if (parameters.count() == 0)
            return emptySequence()

        val currentTypeParam = parameters.map {
            it.type.resolve().declaration
        }
        return currentTypeParam + currentTypeParam.flatMap {
            it.getDependencies()
        }
    }

    private fun KSDeclaration.getComponents(): Sequence<KSClassDeclaration> {
        val types = getDependencies().distinctBy { it.qualifiedName?.asString() }

        val components = types.map {
            when (it) {
                is KSClassDeclaration -> {
                    when {
                        it.hasAnnotation(INJECT.packageName, INJECT.simpleName) -> {
                            it.annotations.first { annotation ->
                                annotation.annotationType.resolve()
                                    .declaration.hasAnnotation(Scope.packageName, Scope.simpleName)
                            }
                                // todo remove this `AssociatedWith` annotation dependency from codebase
                                .annotationType.resolve().declaration.findAnnotation(AssociatedWith::class.qualifiedName.value) as KSClassDeclaration
                        }

                        allProvidersMap[it.qualifiedName?.asString()] != null -> {
                            allProvidersMap[it.qualifiedName?.asString()]
                                ?.findAnnotation(InstallIn::class.qualifiedName.value) as KSClassDeclaration
                        }

                        else -> {
                            throw ClassConstructException("Please mark the class with @Inject or provide it through @Provides")
                        }
                    }
                }

                is KSFunctionDeclaration -> {
                    throw Exception() // todo get parameters type components
                }

                else -> throw Exception()
            }
        }

        return components
    }
}

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