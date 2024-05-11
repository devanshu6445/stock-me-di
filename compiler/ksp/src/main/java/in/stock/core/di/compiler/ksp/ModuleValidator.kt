package `in`.stock.core.di.compiler.ksp

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import `in`.stock.core.di.compiler.core.Messenger
import `in`.stock.core.di.compiler.core.ProcessingStepValidator
import `in`.stock.core.di.compiler.ksp.utils.Scope
import `in`.stock.core.di.compiler.ksp.utils.findAnnotation
import javax.inject.Inject

class ModuleProcessingStepValidatorImpl @Inject constructor(
  private val messenger: Messenger
) : ProcessingStepValidator<KSClassDeclaration> {

  override fun validate(element: KSClassDeclaration): Boolean {
    if (!element.validate()) return false

    if (element.classKind != ClassKind.OBJECT) {
      messenger.error("Module must be a object class", element)
    }

    return element.hasScope()
  }

  private fun KSClassDeclaration.hasScope(): Boolean {
    return runCatching {
      findAnnotation(Scope.canonicalName)
    }.isSuccess
  }
}