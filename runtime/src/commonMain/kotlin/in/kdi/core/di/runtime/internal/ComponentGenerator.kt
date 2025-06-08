package `in`.kdi.core.di.runtime.internal

interface ComponentGenerator<T> {
	fun generateComponent(): T
}