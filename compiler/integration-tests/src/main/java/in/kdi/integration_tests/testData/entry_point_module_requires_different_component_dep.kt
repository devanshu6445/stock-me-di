package `in`.kdi.integration_tests.testData

import `in`.kdi.integration_tests.Dep
import `in`.kdi.runtime.annotations.AssociatedWith
import `in`.kdi.runtime.annotations.EntryPoint
import `in`.kdi.runtime.annotations.InstallIn
import `in`.kdi.runtime.annotations.Module
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