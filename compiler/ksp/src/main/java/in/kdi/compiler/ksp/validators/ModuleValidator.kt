package `in`.kdi.compiler.ksp.validators

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import `in`.kdi.compiler.core.Messenger
import `in`.kdi.compiler.core.ProcessingStepValidator
import `in`.kdi.compiler.ksp.utils.findScope
import javax.inject.Inject

class ModuleValidator @Inject constructor(
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
			findScope()
    }.isSuccess
  }
}