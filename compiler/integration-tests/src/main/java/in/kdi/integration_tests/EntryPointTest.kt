package `in`.kdi.integration_tests

import `in`.kdi.runtime.annotations.*
import `in`.kdi.runtime.annotations.Module
import me.tatarka.inject.annotations.Provides
import me.tatarka.inject.annotations.Scope

@EntryPoint(initializer = "onCreate")
class EntryPointTest : ParentEntryPoint() {

	@Inject
	lateinit var aLazy: Lazy<B>

	@Inject
	lateinit var eDep: EDep

	override fun onCreate() {
		super.onCreate()

		println(a)
	}
}

@Module
@InstallIn(EntryPointTest::class)
@EScope
object EntryPointModule {

	@Provides
	fun provideEDep() = EDep()
}

@Scope
@AssociatedWith(EntryPointTest::class)
annotation class EScope

class EDep

abstract class ParentEntryPoint {

	@Inject
	lateinit var a: Dep

	open fun onCreate() {
	}
}
