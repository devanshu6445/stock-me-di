package `in`.stock.core.di.compiler.ksp

import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import `in`.stock.core.di.compiler.core.Messenger
import `in`.stock.core.di.compiler.core.XResolver
import `in`.stock.core.di.compiler.ksp.exceptions.ClassConstructException
import `in`.stock.core.di.compiler.ksp.utils.*
import `in`.stock.core.di.runtime.annotations.AssociatedWith
import `in`.stock.core.di.runtime.annotations.InstallIn
import `in`.stock.core.di.runtime.annotations.internals.ModuleProvider
import javax.inject.Inject
import javax.inject.Singleton

// todo refactor this to provide different implementation for collecting types for different kotlin elements
@Singleton
class TypeCollector @Inject constructor(
	private val resolver: XResolver,
	private val messenger: Messenger
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
				typeToComponentMap[type.qualifiedName?.asString().orEmpty()] = components.toList()
				components
			}

			else -> {
				TODO()
			}
		}
	}

	private fun KSDeclaration.getDependencies(): Sequence<KSDeclaration> {
		val resolveParameters = when (this) {
			is KSClassDeclaration -> {
				getConstructors().flatMap { it.parameters }.map { it.type } + getAllProperties().filter {
					it.hasAnnotation(
						`in`.stock.core.di.runtime.annotations.Inject::class
					)
				}.map {
					if (it.type.resolve().declaration.qualifiedName?.asString() == "kotlin.Lazy") {
						it.type.resolve().arguments.first().type
							?: messenger.fatalError(IllegalStateException("Generic type information not present for Lazy"), it)
					} else {
						it.type
					}
				}
			}

			is KSFunctionDeclaration -> {
				parameters.map { it.type }.asSequence()
			}

			else -> {
				messenger.fatalError(IllegalArgumentException("This type is not supported by @EntryPoint"), this)
			}
		}.map {
			it.resolve().declaration
		}
		return resolveParameters + resolveParameters.flatMap { it.getDependencies() }
	}

	private fun KSDeclaration.getComponents(): Sequence<KSClassDeclaration> {
		// todo review this code
		val providersDependencies = collectEntryPointProviders(this).flatMap {
			it.getAllFunctions()
				.flatMap { func -> func.extensionReceiver?.resolve()?.declaration?.getDependencies() ?: emptySequence() }
		}.filterNot {
			it.qualifiedName?.asString() == resolver.builtIns.anyType.declaration.qualifiedName?.asString()
		}

		val types = getDependencies().distinctBy { it.qualifiedName?.asString() } + providersDependencies

		return sequence {
			types.forEach {
				messenger.warn("${it.qualifiedName?.asString()}", it)
				when (it) {
					is KSClassDeclaration -> {
						when {
							it.hasAnnotation(INJECT.packageName, INJECT.simpleName) -> {
								val components = typeToComponentMap[qualifiedName?.asString().orEmpty()]

								if (components != null) {
									yieldAll(components)
								} else {
									val component =
										it.annotations.firstOrNull { annotation ->
											annotation.annotationType.resolve()
												.declaration.hasAnnotation(Scope.packageName, Scope.simpleName)
										}
											// todo remove this `AssociatedWith` annotation dependency from codebase
											?.annotationType?.resolve()?.declaration?.getAnnotationArgument(
												AssociatedWith::class.qualifiedName.orEmpty()
											) as? KSClassDeclaration?
									if (component != null) {
										yield(component)
									}
								}
							}

							allProvidersMap[it.qualifiedName?.asString()] != null -> {
								yield(
									allProvidersMap[it.qualifiedName?.asString()]
										?.getAnnotationArgument(InstallIn::class.qualifiedName.orEmpty()) as KSClassDeclaration
								)
							}

							else -> {
								// todo improve logging information
								messenger.fatalError(
									ClassConstructException("Please mark the class with @Inject or provide it through @Provides"),
									it
								)
							}
						}
					}

					is KSFunctionDeclaration -> {
						throw NotImplementedError()
					}

					else -> throw NotImplementedError()
				}
			}
		}
	}

// 	private fun findProviderRelatedTo(type: KSDeclaration): Sequence<KSClassDeclaration> {
// 		return sequence {
// 			resolver.getAllModuleProviders()
// 				.associateBy { it.qualifiedName?.asString() }
// 		}
// 	}

	fun collectEntryPointProviders(type: KSDeclaration): Sequence<KSClassDeclaration> {
		return sequence {
			resolver.getAllModuleProviders().associateBy { a -> a.qualifiedName?.asString() }.values.forEach { provider ->
				if ((provider.getAnnotationNonNull(ModuleProvider::class).arguments.first().value as KSType)
						.declaration.qualifiedName?.asString() == type.qualifiedName?.asString()
				) {
					yield(provider)
				}
			}
		}
	}
}