package main

import `in`.stock.core.di.runtime.annotations.Retriever
import `in`.stock.core.di.runtime.annotations.Component
import `in`.stock.core.di.runtime.annotations.EntryPoint
import `in`.stock.core.di.runtime.SingletonComponent
import `in`.stock.core.di.runtime.RetrieverGetter

@Component
abstract class Comp1(
	@Component val singleton: SingletonComponent
)

@Retriever(component = Comp1::class)
interface Retriever1

@EntryPoint(
	parentComponent = Comp1::class,
	initializer = "onCreate"
)
class EntryPointTest : ParentEntryPoint() {
	override fun onCreate() {

	}
}

abstract class ParentEntryPoint {
	open fun onCreate() {}
}

fun main() {
	RetrieverGetter.get(EntryPointTest(), Retriever1::class)
}
