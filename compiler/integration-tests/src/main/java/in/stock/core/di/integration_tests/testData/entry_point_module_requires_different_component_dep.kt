package `in`.stock.core.di.integration_tests.testData

import `in`.stock.core.di.integration_tests.Dep
import `in`.stock.core.di.runtime.annotations.AssociatedWith
import `in`.stock.core.di.runtime.annotations.EntryPoint
import `in`.stock.core.di.runtime.annotations.InstallIn
import `in`.stock.core.di.runtime.annotations.Module
import me.tatarka.inject.annotations.Provides
import me.tatarka.inject.annotations.Scope

@EntryPoint
class EntryPointDiffComp(
	val dep: EntryPointDiffCompDep
)

@Module
@InstallIn(EntryPointDiffComp::class)
@EntryPointDiffCompScope
object EntryPointDiffCompModule {

	@Provides
	fun provide(dep: Dep) = EntryPointDiffCompDep()
}

@Scope
@AssociatedWith(EntryPointDiffComp::class)
annotation class EntryPointDiffCompScope

class EntryPointDiffCompDep
