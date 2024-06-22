package `in`.stock.core.di.kcp.utils

fun<T> List<T>.asSequence() = Sequence { this.listIterator() }