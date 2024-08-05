package `in`.stock.core.di.integration_tests.testData

import `in`.stock.core.di.integration_tests.Dep
import `in`.stock.core.di.integration_tests.Dep2
import `in`.stock.core.di.integration_tests.Dep3
import `in`.stock.core.di.integration_tests.NewComponent
import `in`.stock.core.di.runtime.SingletonComponent
import `in`.stock.core.di.runtime.annotations.*
import me.tatarka.inject.annotations.Provides
import me.tatarka.inject.annotations.Scope

class Compo1Dep

@Component
@Compo1Scope
abstract class Compo1(
	@Component val singletonComponent: SingletonComponent
)

@Scope
@AssociatedWith(Compo1::class)
annotation class Compo1Scope

@Module
@InstallIn(Compo1::class)
@Compo1Scope
object Compo1Module {

	@Provides
	fun provideCompo1Dep() = Compo1Dep()
}

@EntryPoint(
	parentComponent = NewComponent::class,
	dependencies = [Compo1::class]
)
class EntryPoint123(
	val dep3: Dep3,
	val compo1Dep: Compo1Dep,
	val dep: Dep,
	val dep2: Dep2
)
