package main

import `in`.kdi.runtime.annotations.EntryPoint
import `in`.kdi.runtime.annotations.Inject
import `in`.kdi.runtime.SingletonComponent

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