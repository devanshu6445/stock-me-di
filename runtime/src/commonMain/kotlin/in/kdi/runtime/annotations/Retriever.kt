package `in`.kdi.runtime.annotations

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class Retriever(
	val component: KClass<*>
)
