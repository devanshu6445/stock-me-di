package `in`.stock.core.di.compiler.data

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration

data class ModuleInfo(
  val root: KSClassDeclaration,
  val installInComponent: KSClassDeclaration,
  val scope: KSAnnotation,
  val providers: List<ProvidesInfo>
)
