package `in`.stock.core.di.runtime.annotations

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class InstallIn(
    val component: KClass<*>
)
