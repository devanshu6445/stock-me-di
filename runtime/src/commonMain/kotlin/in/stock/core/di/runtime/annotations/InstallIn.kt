package `in`.stock.core.di.runtime.annotations

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class InstallIn(
    val component: KClass<*>
)
