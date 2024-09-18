package `in`.stock.core.di.runtime.annotations

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class Retriever(
	val component: KClass<*>
)
