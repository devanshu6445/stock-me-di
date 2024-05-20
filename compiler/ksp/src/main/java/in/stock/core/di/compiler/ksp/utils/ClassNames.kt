package `in`.stock.core.di.compiler.ksp.utils

import com.squareup.kotlinpoet.ClassName

private const val AnnotationPackageName = "me.tatarka.inject.annotations"
const val InternalPackage = "in.stock.core.di.internal"

val COMPONENT = ClassName(AnnotationPackageName, "Component")
val INJECT = ClassName(AnnotationPackageName, "Inject")
val Provides = ClassName(AnnotationPackageName, "Provides")
val Scope = ClassName(AnnotationPackageName, "Scope")

val ModuleProviderRegistry = ClassName(InternalPackage, "ModuleProvidersRegistry")

const val LazyName = "kotlin.Lazy"