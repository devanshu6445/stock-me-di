package `in`.kdi.core.di.runtime.annotations

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.BINARY)
annotation class Component(
	val modules: Array<KClass<*>> = []
)
