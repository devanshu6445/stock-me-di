package `in`.stock.core.di.compiler.ksp

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.asClassName
import `in`.stock.core.di.compiler.core.Messenger
import `in`.stock.core.di.compiler.core.XResolver
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
					val parentComponent = getArgument<KSType>(EntryPoint::class, ParentComponentProperty)

					val allComponents = getArrayArgument<KSType>(annotation = EntryPoint::class, name = DependenciesProperty) +
						listOf(parentComponent)

					allComponents
						.map { it.declaration as KSClassDeclaration }
						.forEach { comp ->
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

						// always add the parent component even if it is not used in the component to maintain its hierarchy
						yield(parentComponent.declaration as KSClassDeclaration)
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