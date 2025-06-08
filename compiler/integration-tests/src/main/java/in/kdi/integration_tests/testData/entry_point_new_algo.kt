package `in`.kdi.integration_tests.testData

import `in`.kdi.integration_tests.Dep
import `in`.kdi.integration_tests.Dep2
import `in`.kdi.integration_tests.Dep3
import `in`.kdi.integration_tests.NewComponent
import `in`.kdi.runtime.SingletonComponent
import `in`.kdi.runtime.annotations.*
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
