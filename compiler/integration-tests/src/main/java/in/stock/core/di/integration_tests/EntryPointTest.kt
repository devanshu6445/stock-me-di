package `in`.stock.core.di.integration_tests

import `in`.stock.core.di.runtime.annotations.EntryPoint
import `in`.stock.core.di.runtime.annotations.Inject

@EntryPoint
class EntryPointTest {

  @Inject
  lateinit var a: Dep

  @Inject
  lateinit var aLazy: Lazy<B>
}

@EntryPoint
class PrimaryConstructorEntryPoint(
	val dep: Dep
) {

	@Inject
	lateinit var b: Lazy<B>
}