package main

import `in`.stock.core.di.runtime.annotations.EntryPoint
import `in`.stock.core.di.runtime.annotations.Inject

@EntryPoint
class EntryPointTest(val b: B) {

	@Inject
	lateinit var a: A
}

@me.tatarka.inject.annotations.Inject
class A

@me.tatarka.inject.annotations.Inject
class B

fun main() {
	val entryPoint = EntryPointTest(
		component = EntryPointTestComponent::class.create()
	)
	println(entryPoint.a)
	println(entryPoint.a)
}