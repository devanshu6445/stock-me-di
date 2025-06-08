package `in`.kdi.integration_tests.testData

import `in`.kdi.integration_tests.Dep
import `in`.kdi.integration_tests.NewComponent
import `in`.kdi.runtime.SingletonComponent
import `in`.kdi.runtime.annotations.Component
import `in`.kdi.runtime.annotations.InstallIn
import `in`.kdi.runtime.annotations.Module
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
	fun provideA_Dep(): A_Dep =
        A_Dep()
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