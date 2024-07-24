package `in`.stock.core.di.integration_tests

import `in`.stock.core.di.runtime.annotations.*
import `in`.stock.core.di.runtime.annotations.Module
import me.tatarka.inject.annotations.Provides
import me.tatarka.inject.annotations.Scope

@EntryPoint(initializer = "onCreate", isSuperCalledFirst = true)
class EntryPointTest : ParentEntryPoint() {

	@Inject
	lateinit var aLazy: Lazy<B>

	@Inject
	lateinit var eDep: EDep

// 	@Inject
// 	lateinit var eDep1: EDep1

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

@Module
@InstallIn(EntryPointTest::class)
object EntryPointModule1 {

	@Provides
	fun provideEDep1() = EDep1()
}

class EDep1

class EDep

abstract class ParentEntryPoint {

	@Inject
	lateinit var a: Dep

	open fun onCreate() {
	}
}
