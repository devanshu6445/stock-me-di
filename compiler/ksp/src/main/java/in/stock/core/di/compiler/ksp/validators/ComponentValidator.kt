package `in`.stock.core.di.compiler.ksp.validators

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.validate
import `in`.stock.core.di.compiler.core.ProcessingStepValidator
import `in`.stock.core.di.compiler.ksp.utils.hasAnnotation
import `in`.stock.core.di.runtime.annotations.Component
import javax.inject.Inject

class ComponentValidator @Inject constructor() : ProcessingStepValidator<KSClassDeclaration> {
	override fun validate(element: KSClassDeclaration): Boolean {
		return element.validate() &&
			element.classKind == ClassKind.CLASS &&
			element.modifiers.contains(Modifier.ABSTRACT) &&
			element.primaryConstructor?.let { primaryConstructor ->
				primaryConstructor.parameters.any { it.hasAnnotation(Component::class) }
			} ?: false
	}
}