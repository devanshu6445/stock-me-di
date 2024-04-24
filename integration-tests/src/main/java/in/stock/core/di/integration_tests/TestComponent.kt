package `in`.stock.core.di.integration_tests

import `in`.stock.core.di.runtime.SingletonComponent
import `in`.stock.core.di.runtime.annotations.AssociatedWith
import `in`.stock.core.di.runtime.annotations.Component
import me.tatarka.inject.annotations.Scope


@Component
@TestScope
abstract class TestComponent(
    @Component singletonComponent: SingletonComponent,
    dep3: Dep3
) {
    abstract val dep: Dep
}

@Scope
annotation class TestScope


@Component
@NewScope
abstract class NewComponent {
    abstract val dep3: Dep3
}

@Scope
@AssociatedWith(NewComponent::class)
annotation class NewScope

