package `in`.stock.core.di.integration_tests

import `in`.stock.core.di.runtime.annotations.EntryPoint
import `in`.stock.core.di.runtime.annotations.Inject

@EntryPoint(initializer = "onCreate", isSuperCalledFirst = true)
class EntryPointTest : ParentEntryPoint() {

  @Inject
  lateinit var a: Dep

  @Inject
  lateinit var aLazy: Lazy<B>

	override fun onCreate() {
		super.onCreate()

		println(component)
	}
}

abstract class ParentEntryPoint {

	open fun onCreate() {

	}
}

@EntryPoint
class PrimaryConstructorEntryPoint(
	val dep: Dep
) {

	@Inject
	lateinit var b: Lazy<B>
}