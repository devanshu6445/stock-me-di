package `in`.stock.core.di.kcp.utils

fun <T> List<T>.asSequence() = Sequence { this.listIterator() }

fun <T> MutableList<T>.replaceIf(predicate: (T) -> Boolean, value: (Int, T) -> T): MutableList<T> {
	for ((i, e) in withIndex()) {
		if (predicate(e)) {
			this[i] = value(i, e)
			break
		}
	}
	return this
}