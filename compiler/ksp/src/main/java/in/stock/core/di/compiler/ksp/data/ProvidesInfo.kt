package `in`.stock.core.di.compiler.ksp.data

import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.toClassName
import `in`.stock.core.di.compiler.ksp.utils.IntoMap
import `in`.stock.core.di.compiler.ksp.utils.hasAnnotation

data class ProvidesInfo(
	val reference: KSFunctionDeclaration,
) {
	var parent: ModuleInfo? = null
	val functionName: KSName
		get() = reference.simpleName

	val moduleClass: KSClassDeclaration
		get() = parent!!.root

	val scope: KSAnnotation
		get() = parent!!.scope

	val dependencies: List<KSTypeReference> by lazy {
		reference.parameters.map { it.type }
	}

	val parametersName: List<KSName> by lazy {
		reference.parameters.map { it.name as KSName }
	}
	val resolvedDepType by lazy {
		reference.returnType!!.resolve()
	}

	val providerName by lazy {
		resolvedDepType.toClassName().let {
			ClassName(it.packageName, it.simpleName + "Provider")
		}
	}

	val isCollectedIntoMap: Boolean by lazy {
		reference.hasAnnotation(IntoMap.packageName, IntoMap.simpleName)
	}
}

fun KSFunctionDeclaration.asProvider(): ProvidesInfo {
	return ProvidesInfo(
		reference = this
	)
}