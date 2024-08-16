package `in`.stock.core.di.runtime.internal

interface ComponentGenerator<T> {
	fun generateComponent(): T
}