package `in`.kdi.compiler.ksp.validators

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import `in`.kdi.compiler.core.ProcessingStepValidator
import javax.inject.Inject

const val DependenciesProperty = "dependencies"
const val ParentComponentProperty = "parentComponent"

class EntryPointValidator @Inject constructor() : ProcessingStepValidator<KSDeclaration> {
	override fun validate(element: KSDeclaration): Boolean {
		return when (element) {
			is KSClassDeclaration -> {
				/*element.getArrayArgument(
					annotation = EntryPoint::class,
					name = DEPENDENCIES_PROPERTY
				).isNotEmpty()*/
				true
			}

			else -> {
				false
			}
		}
	}
}