package `in`.stock.core.di.compiler.core

interface Generator<Data : Any, Result : Any> {
    fun generate(data: Data): Result
}