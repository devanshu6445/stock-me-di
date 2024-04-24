package `in`.stock.core.di.compiler.utils

import com.squareup.kotlinpoet.ClassName

private const val ANNOTATION_PACKAGE_NAME = "me.tatarka.inject.annotations"
const val InternalPackage = "in.stock.core.di.internal"

val COMPONENT = ClassName(ANNOTATION_PACKAGE_NAME, "Component")
val INJECT = ClassName(ANNOTATION_PACKAGE_NAME, "Inject")
val Provides = ClassName(ANNOTATION_PACKAGE_NAME, "Provides")
val Scope = ClassName(ANNOTATION_PACKAGE_NAME, "Scope")


val ModuleProviderRegistry = ClassName(InternalPackage, "ModuleProvidersRegistry")