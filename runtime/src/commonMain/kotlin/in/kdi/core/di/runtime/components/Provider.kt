package `in`.kdi.core.di.runtime.components

interface Provider<T> {
  val instance: T

	fun get(): T
}