package main

import `in`.stock.core.di.runtime.annotations.Retriever
import `in`.stock.core.di.runtime.annotations.Component
import `in`.stock.core.di.runtime.SingletonComponent

@Component
abstract class Comp1(
	@Component val comp2: Comp2
)

@Component
abstract class Comp2(
	@Component val singleton: SingletonComponent
)

@Retriever(component = Comp1::class)
interface Retriever1 {
	val comp2_fake: Comp2
}

fun main() {
	val component = Comp1::class.createBoundedComponent()
	val retriever1 = component as Retriever1

	println(retriever1.comp2_fake)
	println(component.comp2)
}