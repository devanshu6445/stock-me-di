package `in`.kdi.core.di.integration_tests.testData

import `in`.stock.core.di.integration_tests.Dep
import `in`.stock.core.di.integration_tests.NewComponent
import `in`.stock.core.di.runtime.SingletonComponent
import `in`.stock.core.di.runtime.annotations.Component
import `in`.stock.core.di.runtime.annotations.InstallIn
import `in`.stock.core.di.runtime.annotations.Module
import me.tatarka.inject.annotations.Provides
import me.tatarka.inject.annotations.Scope

@Scope
annotation class AScope

@Component
@`in`.kdi.core.di.integration_tests.testData.AScope
abstract class A(
	@Component val newComponent: NewComponent,
	val dep: Dep
) {
	abstract val aDep: `in`.kdi.core.di.integration_tests.testData.A_Dep
}

class A_Dep

@Module
@InstallIn(`in`.kdi.core.di.integration_tests.testData.A::class)
@`in`.kdi.core.di.integration_tests.testData.AScope
object AModule {

	@Provides
	fun provideA_Dep(): `in`.kdi.core.di.integration_tests.testData.A_Dep =
        `in`.kdi.core.di.integration_tests.testData.A_Dep()
}

@Component
abstract class B(
	@Component val newComponent: NewComponent,
	@Component val a: `in`.kdi.core.di.integration_tests.testData.A
)

@Component
abstract class C(
	@Component singletonComponent: SingletonComponent,
	@Component b: `in`.kdi.core.di.integration_tests.testData.B
)