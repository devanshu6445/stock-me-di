package `in`.stock.core.di.runtime.annotations

import me.tatarka.inject.annotations.Scope
import kotlin.reflect.KClass

/**
 * ## Will try to remove this.
 * This annotation is to be applied to a [Scope] annotation and currently only used internally by the compiler.
 * It gives the information to the compiler as to which [Component] does this [Scope] belong to.
 * @param kClass [Component] to which this scope is associated with
 */
@Target(AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class AssociatedWith(val kClass: KClass<*>)