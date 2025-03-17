package `in`.stock.core.di.compiler.ksp.data

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import `in`.stock.core.di.compiler.ksp.ext.getArgument
import `in`.stock.core.di.compiler.ksp.utils.Provides
import `in`.stock.core.di.compiler.ksp.utils.findScope
import `in`.stock.core.di.compiler.ksp.utils.hasAnnotation
import `in`.stock.core.di.runtime.annotations.InstallIn

data class ModuleInfo(
	val root: KSClassDeclaration,
	val installInComponent: KSClassDeclaration,
	val scope: KSAnnotation,
	val providers: List<ProvidesInfo>
)

fun KSClassDeclaration.asModule(): ModuleInfo {
	return ModuleInfo(
		root = this,
		installInComponent = getArgument<KSType>(
			annotation = InstallIn::class,
			name = "component"
		).declaration as KSClassDeclaration,
		scope = findScope(),
		providers = getAllFunctions()
			.filter { it.hasAnnotation(Provides.packageName, Provides.simpleName) }
			.map { it.asProvider() }.toList() // due to an error of not able to assign parent using sequence
	).apply {
		providers.forEach { it.parent = this }
	}
}
