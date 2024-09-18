package `in`.stock.core.di.compiler.ksp.generators

import com.github.adriankuta.datastructure.tree.TreeNode
import com.github.adriankuta.datastructure.tree.iterators.TreeNodeIterators
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.plusParameter
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile
import com.squareup.kotlinpoet.ksp.toClassName
import `in`.stock.core.di.compiler.core.Generator
import `in`.stock.core.di.compiler.core.Messenger
import `in`.stock.core.di.compiler.core.XCodeGenerator
import `in`.stock.core.di.compiler.core.XResolver
import `in`.stock.core.di.compiler.core.ext.writeTo
import `in`.stock.core.di.compiler.ksp.data.ComponentGeneratorResult
import `in`.stock.core.di.compiler.ksp.data.ComponentInfo
import `in`.stock.core.di.compiler.ksp.ext.getArgument
import `in`.stock.core.di.compiler.ksp.utils.*
import `in`.stock.core.di.runtime.SingletonComponent
import `in`.stock.core.di.runtime.annotations.Component
import `in`.stock.core.di.runtime.annotations.internals.ModuleProvider
import javax.inject.Inject
import kotlin.reflect.KClass

class ComponentGenerator @Inject constructor(
	private val xCodeGenerator: XCodeGenerator,
	private val xResolver: XResolver,
	private val messenger: Messenger
) :
	Generator<ComponentInfo, ComponentGeneratorResult> {
	override fun generate(data: ComponentInfo): ComponentGeneratorResult {
		val name = data.generatedName

		val parentComponents = data.parentComponents.map { it.canonicalName }

		// Filter out all the providers from previous rounds od ksp processing as well as from the other
		// todo only pick the dependent modules providers
		//  as `data.moduleProviders` contains all the ModuleProvider of this compilation round
		val providersToImplement = xResolver.getAllModuleProviders()
			.filter {
				it.getArgument<KSType>(ModuleProvider::class, "clazz").let { moduleProvider ->
					messenger.warn("${data.root} -> $parentComponents $it $moduleProvider")
					parentComponents.contains(moduleProvider.declaration.qualifiedName?.asString()) ||
						moduleProvider.declaration.qualifiedName?.asString() == data.root.qualifiedName?.asString()
				}
			}.map { it.toClassName() }

		FileSpec.builder(name)
			.addType(
				TypeSpec.classBuilder(name)
					.apply {
						data.root.containingFile?.let { addOriginatingKSFile(it) }
					}
					.addAnnotation(COMPONENT)
					.superclass(data.root.toClassName())
					.apply {
						(data.parentComponents + data.dependencies).map {
							CodeBlock.of(
								format = "%N",
								ParameterSpec(
									name = it.simpleName.replaceFirstChar { char -> char.lowercaseChar() } + "2",
									type = it
								)
							)
						}.forEach {
							addSuperclassConstructorParameter(it)
						}
					}
					.addSuperinterfaces(data.providersToImplement + providersToImplement)
					.constructorBuilder(
						parentComponent = data.parentComponents,
						dependencies = data.dependencies
					)
					.addModifiers(KModifier.ABSTRACT)
					.build()
			)
			.generateCreatorFunction(
				root = data.root,
				generatedComponentName = data.generatedName
			)
			.apply {
				addFunction(createCreatorFunction(componentFileBuilder = this, data.root))
			}
			.build().writeTo(xCodeGenerator)

		return ComponentGeneratorResult(name = name)
	}

	@Suppress("UnusedPrivateMember")
	private fun FileSpec.Builder.generateCreatorFunction(
		root: KSClassDeclaration,
		generatedComponentName: ClassName,
	) = apply {
		addFunction(
			FunSpec.builder(MemberName(packageName, "create"))
				.apply {
					root.containingFile?.let { addOriginatingKSFile(it) }
				}
				.receiver(KClass::class.asTypeName().plusParameter(root.toClassName()))
				.addParameters(
					root.primaryConstructor?.parameters?.map { param ->
						ParameterSpec.builder(
							param.name?.asString().orEmpty(),
							param.type.resolve().toClassName()
						).build()
					} ?: emptyList()
				)
				.returns(root.toClassName())
				.addStatement(
					"return %T::class.%T(${
						buildString {
							root.primaryConstructor?.parameters?.forEach {
								append("${it.name?.asString()},")
							}
						}
					})",
					generatedComponentName,
					ClassName(
						packageName = generatedComponentName.packageName,
						"create"
					)
				)
				.build()
		)
	}

	private fun generateComponentTree(declaration: KSDeclaration): TreeNode<KSDeclaration> {
		val currentNode = TreeNode(declaration, TreeNodeIterators.PostOrder)

		when (declaration) {
			is KSClassDeclaration -> {
				declaration.primaryConstructor?.parameters?.forEach {
					val resolvedDeclaration = it.type.resolve().declaration
					if (it.hasAnnotation(Component::class)) {
						currentNode.addChild(generateComponentTree(resolvedDeclaration))
					} else {
						currentNode.addChild(
							TreeNode(resolvedDeclaration, TreeNodeIterators.PostOrder)
						)
					}
				}
			}

			else -> {
				currentNode.addChild(
					TreeNode(declaration, TreeNodeIterators.PostOrder)
				)
			}
		}
		return currentNode
	}

	private fun createCreatorFunction(
		componentFileBuilder: FileSpec.Builder,
		root: KSClassDeclaration
	): FunSpec {
		val componentTree = generateComponentTree(root)

		return FunSpec.builder(MemberName(root.toClassName().packageName, "createBoundedComponent"))
			.apply { root.containingFile?.let { addOriginatingKSFile(it) } }
			.receiver(KClass::class.asTypeName().plusParameter(root.toClassName()))
			.returns(root.toClassName())
			.addParameters(
				componentTree.filterNot {
					it.value.hasAnnotation(Component::class) || it.value.hasAnnotation(
						COMPONENT.packageName,
						COMPONENT.simpleName
					)
				}.map {
					val depType = it.value as? KSClassDeclaration

					if (depType != null) {
						val typeName = depType.toClassName()

						ParameterSpec.builder(name = it.value.simpleName.asString().camelCase(), type = typeName)
							.build()
					} else {
						error("Not supported")
					}
				}
			)
			.addCode(
				createComponentCreatorBlock(
					root = root,
					componentFileBuilder = componentFileBuilder,
					componentTree = componentTree
				)
			)
			.addStatement("return ${root.toClassName().simpleName.camelCase()}")
			.build()
	}

	@Suppress("SpreadOperator")
	private fun createComponentCreatorBlock(
		root: KSClassDeclaration,
		componentFileBuilder: FileSpec.Builder,
		componentTree: TreeNode<KSDeclaration>
	): CodeBlock {
		val addedComponents = mutableMapOf<String, KSDeclaration>()
		// TypeName list to feed to CodeBlock for type resolution
		val typeList = mutableListOf<TypeName>()

		return CodeBlock.of(
			format = buildString {
				componentTree.forEach {
					val component = it.value
					if (component is KSClassDeclaration) {
						if (!addedComponents.containsKey(component.qualifiedName?.asString().orEmpty())) {
							if (component.qualifiedName?.asString() == SingletonComponent::class.qualifiedName) {
								appendLine("val ${component.simpleName.asString().camelCase()} = %T.getInstance()")
								typeList.add(component.toClassName())
							} else {
								// skip the actual assisted parameter dependency as that is already added in the defined function
								// add it to the `addedComponents` map
								if (component.hasAnnotation(Component::class)) {
									val componentParameters = component.primaryConstructor?.parameters?.map { param ->
										val resolvedParam = param.type.resolve().declaration
										val alreadyAddedComponent = addedComponents[resolvedParam.qualifiedName?.asString()]
										if (alreadyAddedComponent != null) {
											resolvedParam.simpleName.asString().camelCase()
										} else {
											error("Should have never encountered this condition")
										}
									} ?: emptyList()

									appendLine(
										"val ${component.simpleName.asString().camelCase()} = %T::class.create(${
											componentParameters.toString().removePrefix("[").removeSuffix("]")
										})"
									)

									when (component.packageName.asString()) {
										root.packageName.asString() -> {
											Unit
										}

										SingletonComponent::class.qualifiedName -> {
											componentFileBuilder.addImport(SingletonComponent::class)
											componentFileBuilder.addImport(SingletonComponent::class.asClassName().packageName, "create")
										}

										else -> {
											componentFileBuilder.addImport(
												component.packageName.asString(),
												"Generated${component.simpleName.asString()}"
											)

											componentFileBuilder.addImport(
												component.packageName.asString(),
												"create"
											)
										}
									}
									typeList.add(
										ClassName(
											packageName = component.packageName.asString(),
											"Generated${component.simpleName.asString()}"
										)
									)
								}
							}
							addedComponents[component.qualifiedName?.asString().orEmpty()] = component
						}
					}
				}
			},
			*typeList.toTypedArray()
		)
	}

	private fun TypeSpec.Builder.constructorBuilder(
		parentComponent: List<ClassName>,
		dependencies: List<ClassName>
	) = apply {
		val constructorBuilder = FunSpec.constructorBuilder()

		parentComponent.forEach { component ->
			// todo Can look into using NameAllocator to allocate non-collision names
			val name = component.simpleName.replaceFirstChar { it.lowercaseChar() } + "2"

			// adding the parameter as with val due to val check in kotlin-inject library
			constructorBuilder.addConstructorProperty(
				typeSpec = this,
				name = name,
				type = component,
				annotations = listOf(COMPONENT.toAnnotationSpec())
			)
		}

		dependencies.forEach { dependency ->
			val name = dependency.simpleName.replaceFirstChar { it.lowercaseChar() } + "2"

			constructorBuilder.addConstructorProperty(
				typeSpec = this,
				name = name,
				type = dependency,
			)
		}

		primaryConstructor(constructorBuilder.build())
	}
}