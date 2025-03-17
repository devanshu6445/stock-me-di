package main

import `in`.stock.core.di.runtime.annotations.EntryPoint
import `in`.stock.core.di.runtime.annotations.Inject
import `in`.stock.core.di.runtime.SingletonComponent

@EntryPoint
class EntryPointTest {

	@Inject
	lateinit var a: A
}

@me.tatarka.inject.annotations.Inject
class A

fun main() {
	val entryPoint = EntryPointTest(
		component = EntryPointTestComponent::class.create(SingletonComponent.getInstance())
	)
	println(entryPoint.a)
	println(entryPoint.a)
}