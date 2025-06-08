package `in`.kdi.runtime

import `in`.kdi.runtime.SingletonComponent.Companion.getInstance
import `in`.kdi.runtime.annotations.AssociatedWith
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.KmpComponentCreate
import me.tatarka.inject.annotations.Scope

/**
 * The application-level scope. There will only be one instance of anything annotated with this.
 */
@Scope
@AssociatedWith(`in`.kdi.runtime.SingletonComponent::class)
annotation class Singleton

/**
 * The main application component. Use [getInstance] to ensure the same instance is shared.
 */
@Component
@`in`.kdi.runtime.Singleton
abstract class SingletonComponent {
    companion object {
        private var instance: `in`.kdi.runtime.SingletonComponent? = null

        /**
         * Get a singleton instance of [SingletonComponent].
         */
        fun getInstance(): `in`.kdi.runtime.SingletonComponent = `in`.kdi.runtime.SingletonComponent.Companion.instance
            ?: `in`.kdi.runtime.createSingletonComponent()
                .also { `in`.kdi.runtime.SingletonComponent.Companion.instance = it }
    }
}

@KmpComponentCreate
expect fun createSingletonComponent(): `in`.kdi.runtime.SingletonComponent