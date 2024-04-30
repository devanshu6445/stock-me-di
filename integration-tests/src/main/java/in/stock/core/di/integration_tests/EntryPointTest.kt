package `in`.stock.core.di.integration_tests

import `in`.stock.core.di.runtime.annotations.EntryPoint

@EntryPoint
fun functionComponentTest(
    dep: Dep
) {
}

@EntryPoint
class EntryPointTest(
    private val dep: Dep
)