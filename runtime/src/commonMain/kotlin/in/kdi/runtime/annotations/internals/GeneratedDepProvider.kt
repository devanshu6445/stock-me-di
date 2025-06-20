package `in`.kdi.runtime.annotations.internals

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class GeneratedDepProvider(
	val clazz: KClass<*>
)