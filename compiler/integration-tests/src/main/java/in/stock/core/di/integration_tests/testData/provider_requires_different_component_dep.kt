package `in`.stock.core.di.integration_tests.testData

import `in`.stock.core.di.integration_tests.Dep
import `in`.stock.core.di.runtime.SingletonComponent
import `in`.stock.core.di.runtime.annotations.Component
import `in`.stock.core.di.runtime.annotations.InstallIn
import `in`.stock.core.di.runtime.annotations.Module
import me.tatarka.inject.annotations.Provides
import me.tatarka.inject.annotations.Scope

// When a provider itself requires the dependency from other component
// https://develope.youtrack.cloud/issue/DI-1

@Component
@Comp1Scope
abstract class Comp1(
	@Component val singletonComponent: SingletonComponent
) {
	abstract val dep1: Dep1
}

class Dep1

@Scope
annotation class Comp1Scope

@Module
@InstallIn(Comp1::class)
@Comp1Scope
object Comp1M1 {
	@Provides
	fun provide(dep: Dep): Dep1 {
		return Dep1()
	}
}
