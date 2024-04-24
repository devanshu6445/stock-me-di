package `in`.stock.core.di.runtime.annotations.internals

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class ModuleProvider(
    val clazz: KClass<*>
)
