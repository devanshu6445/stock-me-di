package `in`.stock.core.di.runtime.annotations.internals

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class Aggregated(
	val aggregationOf: KClass<*>,
	val topLevelElement: KClass<*>
)
