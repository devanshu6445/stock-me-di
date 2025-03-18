package `in`.stock.core.di.integration_tests.testData

import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import me.tatarka.inject.annotations.Scope


@Component
@DuplicateComp1Scope
abstract class DuplicateComp1(
	@Component val parent : DuplicateComp1ParentComp,
	val dep2: Comp1Dep2
) : DuplicateComp1Provider {

	abstract val dep: Comp1Dep1
}

@Scope
annotation class DuplicateComp1Scope

@Scope
annotation class DuplicateComp1ParentCompScope

@Component
@DuplicateComp1ParentCompScope
abstract class DuplicateComp1ParentComp {

}

class Comp1Dep1

class Comp1Dep2

interface DuplicateComp1Provider {

	@Provides
	fun provideComp1Dep1() = Comp1Dep1()
}

interface Provider {

	@Provides
	fun provideComp1Dep1() = Comp1Dep1()
}