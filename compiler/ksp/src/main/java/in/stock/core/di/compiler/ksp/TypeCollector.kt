package `in`.stock.core.di.compiler.ksp

import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.asClassName
import `in`.stock.core.di.compiler.core.Messenger
import `in`.stock.core.di.compiler.core.XResolver
import `in`.stock.core.di.compiler.ksp.exceptions.ClassConstructException
import `in`.stock.core.di.compiler.ksp.ext.eqv
import `in`.stock.core.di.compiler.ksp.ext.getAllFunctionExceptPrimitive
import `in`.stock.core.di.compiler.ksp.ext.getArgument
import `in`.stock.core.di.compiler.ksp.ext.getArrayArgument
import `in`.stock.core.di.compiler.ksp.utils.*
import `in`.stock.core.di.compiler.ksp.validators.DependenciesProperty
import `in`.stock.core.di.compiler.ksp.validators.ParentComponentProperty
import `in`.stock.core.di.runtime.SingletonComponent
import `in`.stock.core.di.runtime.annotations.*
import `in`.stock.core.di.runtime.annotations.internals.GeneratedDepProvider
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
		val types = getDependencies().distinctBy { it.qualifiedName?.asString() }

		return sequence {
			yield(
				getArgument<KSType>(EntryPoint::class, name = ParentComponentProperty)
					.declaration as KSClassDeclaration
			)

			yieldAll(
				getArrayArgument<KSType>(annotation = EntryPoint::class, name = DependenciesProperty).map {
					it.declaration as KSClassDeclaration
				}
			)

			types.forEach {
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

	@Suppress("CyclomaticComplexMethod", "NestedBlockDepth")
	fun findRequiredComponents(
		rootNode: KSDeclaration,
	): Pair<Sequence<KSClassDeclaration>, Sequence<KSClassDeclaration>> {
		val componentsTree = hashMapOf<String, KSClassDeclaration>()

		fun KSDeclaration.gatherComponents() {
			when (this) {
				is KSClassDeclaration -> {
					if (primaryConstructor?.parameters?.size == 1 && primaryConstructor!!.parameters.first().type.resolve()
							.declaration.qualifiedName?.asString() == SingletonComponent::class.qualifiedName
					) {
						primaryConstructor!!.parameters.first().type.resolve().declaration.let {
							componentsTree[it.qualifiedName?.asString().orEmpty()] = it as KSClassDeclaration
						}

						if (this.qualifiedName?.asString() != rootNode.qualifiedName?.asString()) {
							componentsTree[qualifiedName?.asString().orEmpty()] = this
						}

						return
					}

					primaryConstructor?.parameters?.filter { it.hasAnnotation(Component::class) }
						?.map { it.type.resolve().declaration }?.forEach { comp ->
							comp.gatherComponents()
							componentsTree[qualifiedName?.asString().orEmpty()] = this
						}

					componentsTree[qualifiedName?.asString().orEmpty()] = this
				}
			}
		}

		fun KSClassDeclaration.getScopedComponent() =
			findScope().annotationType.resolve().declaration.getArgument<KSType>(
				annotation = AssociatedWith::class,
				name = "kClass"
			)

		when (rootNode) {
			is KSClassDeclaration -> {
				with(rootNode) {
					(
						getArrayArgument<KSType>(
						annotation = EntryPoint::class,
							name = DependenciesProperty
					) + listOf(
						getArgument(
							EntryPoint::class,
							ParentComponentProperty
						)
						)
						).map { it.declaration as KSClassDeclaration }.forEach { comp ->
						comp.gatherComponents()
					}

					val dependencyToComponentMap = hashMapOf<String, KSClassDeclaration>()

					componentsTree.forEach { entry ->
						findModules(entry.value).flatMap { module ->
							module.getAllFunctionExceptPrimitive().map { func -> func.returnType!! }
						}.forEach {
							dependencyToComponentMap[it.resolve().declaration.qualifiedName?.asString().orEmpty()] = entry.value
						}
					}

					val generatedDepProviderClassName = GeneratedDepProvider::class.asClassName()

					val entryPointModules = findModules(rootNode)

					val moduleForThisComponent = entryPointModules
						.flatMap {
							it.getAllFunctions().map { func -> func.returnType!!.resolve().declaration.qualifiedName?.asString() }
						}.toMutableSet()

					val propertiesWhichRequireProvider = getAllProperties().filterNot {
						moduleForThisComponent.contains(it.type.resolve().declaration.qualifiedName?.asString())
					}.associateBy { it.type.resolve().declaration.qualifiedName?.asString() }

					val directlyRequiredComponents = sequence {
						resolver.getSymbolsWithClassAnnotation(
							packageName = generatedDepProviderClassName.packageName,
							simpleName = generatedDepProviderClassName.simpleName
						).forEach { depProvider ->
							depProvider.getArgument<KSType>(GeneratedDepProvider::class, "clazz")
								.let { arg ->
									val qualifiedName = arg.declaration.qualifiedName?.asString()
									if (propertiesWhichRequireProvider.containsKey(qualifiedName)) {
										// todo can throw error if the properties declared in the entry point required
										//  other component's dependency but the dependent component is not added as dependency to the entry point
										componentsTree.getOrDefault(
											depProvider.getScopedComponent().declaration.qualifiedName?.asString(),
											null
										)?.let { componentDeclaration ->
											yield(componentDeclaration)
										}
									}
								}
						}
					}

					val directlyRequiredModules = directlyRequiredComponents.flatMap {
						findModules(it)
					}

					val transitivelyRequiredComponents = sequence {
						(entryPointModules + directlyRequiredModules).flatMap {
							it.getAllFunctionExceptPrimitive().flatMap { func -> func.parameters }
						}
							.filterNot { it.isPrimitive || it.type eqv resolver.builtIns.anyType || it.type eqv resolver.builtIns.unitType }
							.map { it.type.resolve() to it }
							.distinctBy { it.first.declaration.qualifiedName?.asString() }.forEach {
								val resolvedType = it.first.declaration as KSClassDeclaration
								try {
									yield(resolvedType.getScopedComponent().declaration as KSClassDeclaration)
								} catch (e: NoSuchElementException) {
									// ignore exception only if the dependency is not bound to any scope this

									dependencyToComponentMap[resolvedType.qualifiedName?.asString()]?.let { component ->
										yield(component)
									} ?: run {
										if (!resolvedType.hasAnnotation(INJECT.packageName, INJECT.simpleName)) {
											messenger.fatalError(
												Exception("Please mark the class with @Inject or provide it through a @Provides $dependencyToComponentMap"),
												it.second
											)
										}
									}
								}
							}
					}

					val transitivelyRequiredModules = transitivelyRequiredComponents.flatMap {
						findModules(it)
					}

					// todo components are being duplicated in `transitivelyRequiredComponents`,
					//  check this and remove `distinctBy` from here
					val componentsSequence = sequence {
						yieldAll(directlyRequiredComponents)
						yieldAll(transitivelyRequiredComponents)
					}
						.distinctBy { it.qualifiedName?.asString() }

					val modulesSequence = sequence {
						yieldAll(directlyRequiredModules)
						yieldAll(transitivelyRequiredModules)

						yieldAll(findModules(rootNode))
					}
						.distinctBy { it.qualifiedName?.asString() }

					return componentsSequence to modulesSequence
				}
			}

			else -> return emptySequence<KSClassDeclaration>() to emptySequence()
		}
	}

	/**
	 * Finds the modules related to the [type] component.
	 * @param type For which modules should be queried
	 */
	private fun findModules(type: KSClassDeclaration): Sequence<KSClassDeclaration> = sequence {
		resolver.getSymbolsWithClassAnnotation(
			Module::class
		).forEach {
			if (it.getArgument<KSType>(
					InstallIn::class,
					"component"
				).declaration.qualifiedName?.asString() == type.qualifiedName?.asString()
			) {
				yield(it)
			}
		}
	}

	fun findModuleProvidersByModule(modules: Sequence<KSClassDeclaration>): Sequence<KSClassDeclaration> {
		val allProviders = resolver.getSymbolsWithClassAnnotation(
			ModuleProvider::class
		)

		return modules.flatMap {
			findModuleProvidersByModule(
				module = it,
				moduleProviders = allProviders
			)
		}
	}

	private fun findModuleProvidersByModule(
		module: KSClassDeclaration,
		moduleProviders: Sequence<KSClassDeclaration>
	): Sequence<KSClassDeclaration> = sequence {
		val installInComponent = module.getArgument<KSType>(
			annotation = InstallIn::class,
			name = "component"
		)

		moduleProviders.forEach {
			if (it.getArgument<KSType>(
					annotation = ModuleProvider::class,
					name = "clazz"
				).declaration.qualifiedName?.asString() == installInComponent.declaration.qualifiedName?.asString()
			) {
				yield(it)
			}
		}
	}
}
