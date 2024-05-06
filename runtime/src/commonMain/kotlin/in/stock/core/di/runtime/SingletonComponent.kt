package `in`.stock.core.di.runtime

import `in`.stock.core.di.runtime.SingletonComponent.Companion.getInstance
import `in`.stock.core.di.runtime.annotations.AssociatedWith
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Scope

/**
 * The application-level scope. There will only be one instance of anything annotated with this.
 */
@Scope
@AssociatedWith(SingletonComponent::class)
annotation class Singleton

/**
 * The main application component. Use [getInstance] to ensure the same instance is shared.
 */
@Component
@Singleton
abstract class SingletonComponent {
    companion object {
        private var instance: SingletonComponent? = null

        /**
         * Get a singleton instance of [SingletonComponent].
         */
        fun getInstance() = instance ?: SingletonComponent::class.create(
        ).also { instance = it }
    }
}