package `in`.stock.core.di.integration_tests

import `in`.stock.core.di.runtime.annotations.EntryPoint
import `in`.stock.core.di.runtime.annotations.Inject

//@EntryPoint
fun functionComponentTest(
  dep: Dep
) {

}

@EntryPoint
class EntryPointTest(val dep: Dep) {

  @Inject
  lateinit var a: Dep
}