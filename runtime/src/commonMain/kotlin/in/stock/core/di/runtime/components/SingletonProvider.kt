package `in`.stock.core.di.runtime.components

abstract class SingletonProvider<T : Any>(
    private val provider: () -> T,
) : Provider<T> {

    override val instance by lazy {
        provider()
    }
}