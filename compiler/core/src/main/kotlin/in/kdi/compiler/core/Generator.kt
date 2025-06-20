package `in`.kdi.compiler.core

interface Generator<Data : Any, Result : Any> {
  fun generate(data: Data): Result
}