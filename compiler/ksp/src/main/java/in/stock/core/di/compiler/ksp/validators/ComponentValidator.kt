package `in`.stock.core.di.compiler.ksp.validators

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.validate
import `in`.stock.core.di.compiler.core.ProcessingStepValidator
import javax.inject.Inject

class ComponentValidator @Inject constructor() : ProcessingStepValidator<KSClassDeclaration> {
	override fun validate(element: KSClassDeclaration): Boolean {
		// can see if a check can be added to ensure the hierarchy of the component
		return element.validate() &&
			element.classKind == ClassKind.CLASS &&
			element.modifiers.contains(Modifier.ABSTRACT)
	}
}
