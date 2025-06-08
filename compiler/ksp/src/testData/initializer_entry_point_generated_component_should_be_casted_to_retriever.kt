package main

import `in`.kdi.runtime.annotations.Retriever
import `in`.kdi.runtime.annotations.Component
import `in`.kdi.runtime.annotations.EntryPoint
import `in`.kdi.runtime.SingletonComponent
import `in`.kdi.runtime.RetrieverGetter

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
