package `in`.stock.core.di.integration_tests

import `in`.stock.core.di.runtime.annotations.EntryPoint

@EntryPoint
class EntryPointTest(
    val depA: DepB,
    val dep: Dep,
    val dep2: Dep2
) {
    init {
        val component = EntryPointTestComponent::class.create()
    }
}


@EntryPoint
fun A(dep: Dep, dep2: Dep2) {
    val comp = AComponent::class.create()
}