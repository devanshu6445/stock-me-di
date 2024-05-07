package `in`.stock.core.di.kotlin_di_compiler.utils

fun<T> List<T>.asSequence() = Sequence { this.listIterator() }