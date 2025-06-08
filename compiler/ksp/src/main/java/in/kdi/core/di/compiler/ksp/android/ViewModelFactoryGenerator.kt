package `in`.kdi.core.di.compiler.ksp.android

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import `in`.stock.core.di.compiler.core.XRoundEnv
import `in`.stock.core.di.compiler.ksp.EntryPointProcessor
import `in`.stock.core.di.compiler.ksp.utils.ViewModel
import javax.inject.Inject

class ViewModelFactoryGenerator @Inject constructor() : EntryPointProcessor {
	override fun isApplicable(
		xRoundEnv: XRoundEnv,
		node: KSDeclaration
	): Boolean {
		return node is KSClassDeclaration &&
			node.superTypes.any { it.resolve().declaration.qualifiedName?.asString() == ViewModel }
	}

	override fun process(
		xRoundEnv: XRoundEnv,
		node: KSDeclaration
	) {
// 		val viewModelFactoryName = "${node.simpleName.asString()}Factory"
// 		FileSpec.builder(
// 			className = ClassName(
// 				packageName = node.packageName.asString(),
// 				viewModelFactoryName
// 			)
// 		).addType(
// 			TypeSpec.classBuilder(
// 				name = viewModelFactoryName
// 			)
// 				.superclass(
// 					ClassName()
// 				)
// 				.build()
// 		)
	}
}