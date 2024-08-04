package `in`.stock.core.di.integration_tests.testData

import `in`.stock.core.di.integration_tests.Dep
import `in`.stock.core.di.integration_tests.NewComponent
import `in`.stock.core.di.runtime.SingletonComponent
import `in`.stock.core.di.runtime.annotations.Component

@Component
abstract class A(
	@Component val newComponent: NewComponent,
	val dep: Dep
)

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
