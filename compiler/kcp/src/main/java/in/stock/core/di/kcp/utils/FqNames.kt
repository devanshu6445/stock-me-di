package `in`.stock.core.di.kcp.utils

import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

object FqNames {
  private const val KotlinInjectAnnotationPackageName = "me.tatarka.inject.annotations"
  private const val KotlinInject = "$KotlinInjectAnnotationPackageName.Inject"

  private fun classIdFor(cname: String) =
    ClassId(FqName("in.stock.core.di.runtime.annotations"), Name.identifier(cname))

  val EntryPoint = FqName("in.stock.core.di.runtime.annotations.EntryPoint")

  val EntryPointClassId = classIdFor("EntryPoint")

  val Inject = classIdFor("Inject").asSingleFqName()
}