package `in`.kdi.runtime.components

interface Provider<T> {
  val instance: T

	fun get(): T
}