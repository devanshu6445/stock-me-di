package `in`.stock.core.di.integration_tests

import `in`.stock.core.di.runtime.SingletonComponent
import `in`.stock.core.di.runtime.annotations.AssociatedWith
import `in`.stock.core.di.runtime.annotations.Component
import `in`.stock.core.di.runtime.annotations.InstallIn
import `in`.stock.core.di.runtime.annotations.Module
import me.tatarka.inject.annotations.Provides
import me.tatarka.inject.annotations.Scope

@Component
@TestScope
abstract class TestComponent(
  @Component singletonComponent: SingletonComponent,
) {
  abstract val dep: Dep
}

class TestDep

@Scope
@AssociatedWith(TestComponent::class)
annotation class TestScope

@Module
@InstallIn(TestComponent::class)
@TestScope
object TestCompModule {

	@Provides
	fun provideTestDep() = TestDep()
}

@Component
@NewScope
abstract class NewComponent(
	@Component val singletonComponent: SingletonComponent
) {
  abstract val dep3: Dep3

	abstract val dep: Dep
}

@Scope
@AssociatedWith(NewComponent::class)
annotation class NewScope
