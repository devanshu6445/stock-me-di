package `in`.kdi.core.di.runtime.annotations

import `in`.kdi.core.runtime.SingletonComponent
import kotlin.reflect.KClass

// todo add other method as initializer support
/**
 * This annotation is used to create a separate [Component] for the marked entity.
 * The generated [Component] will be bound to the lifecycle of the marked entity.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class EntryPoint(
	val parentComponent: KClass<*> = SingletonComponent::class,
	val dependencies: Array<KClass<*>> = [],
	val initializer: String = "constructor"
)