package `in`.stock.core.di.runtime.annotations

/**
 * This annotation is used to create a separate [Component] for the marked entity.
 * The generated [Component] will be bound to the lifecycle of the marked entity.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class EntryPoint