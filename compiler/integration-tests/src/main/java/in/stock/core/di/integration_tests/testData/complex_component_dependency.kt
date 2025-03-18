package `in`.stock.core.di.integration_tests.testData

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
@AScope
abstract class A(
	@Component val newComponent: NewComponent,
	val dep: Dep
) {
	abstract val aDep: A_Dep
}

class A_Dep

@Module
@InstallIn(A::class)
@AScope
object AModule {

	@Provides
	fun provideA_Dep(): A_Dep = A_Dep()
}

@Component
abstract class B(
	@Component val newComponent: NewComponent,
	@Component val a: A
)

@Component
abstract class C(
	@Component singletonComponent: SingletonComponent,
	@Component b: B
)