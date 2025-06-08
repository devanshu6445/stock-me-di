package `in`.kdi.compiler.ksp.data

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName
import `in`.kdi.compiler.ksp.ext.getArgument
import `in`.kdi.compiler.ksp.utils.hasAnnotation
import `in`.kdi.runtime.SingletonComponent
import `in`.kdi.runtime.annotations.Component
import kotlin.reflect.KClass

data class ComponentInfo(
	val root: KSClassDeclaration,
	val modules: List<ModuleInfo>,
	val modulesProvider: List<ModuleProviderResult>
) {
	val generatedName by lazy {
		ClassName(root.toClassName().packageName, "Generated${root.simpleName.asString()}")
	}

	private val constructorParameters by lazy {
		root.primaryConstructor?.parameters ?: listOf()
	}

	// todo can use partition for `parentComponents` and `dependencies`
	val parentComponentDeclarations by lazy {
		constructorParameters.filter { it.hasAnnotation(Component::class) }
			.map {
				it
			}
	}

	val parentComponents by lazy {
		parentComponentDeclarations.map {
			it.type.resolve().toClassName().let {
				if (it.canonicalName == SingletonComponent::class.qualifiedName) {
					it
				} else {
					ClassName(it.packageName, "Generated${it.simpleName}")
				}
			}
		}
	}

	val dependencies by lazy {
		constructorParameters.filter { !it.hasAnnotation(Component::class) }
			.map { it.type.resolve().toClassName() }
	}

	val providersToImplement by lazy {
		val parentComponents = parentComponents.map { it.canonicalName }
		val arguments = parentComponentDeclarations.associate {
			it.type.resolve().declaration.qualifiedName?.asString() to it.getArgument<List<KClass<*>>>(
				annotation = Component::class,
				name = "modules"
			).map {
				it.asClassName()
			}
		}


		modulesProvider.filter {
			// check whether this Module Provider is installed/created for any of the parent components or
			// for this component(For which we are generating this component) itself
			val installIn = it.installingComponent.toClassName().canonicalName
			val installingComponentArgs = arguments[installIn]

			root.qualifiedName?.asString() == installIn || (parentComponents.contains(installIn) && installingComponentArgs?.contains(
				it.name
			) == false)
		}.map { it.name }
	}
}