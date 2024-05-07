package `in`.stock.core.di.compiler.data

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName

data class ModuleProviderResult(
  val name: ClassName,
  val installingComponent: KSClassDeclaration
)