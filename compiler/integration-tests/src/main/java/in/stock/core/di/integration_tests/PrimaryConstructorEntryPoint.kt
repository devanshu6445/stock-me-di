package `in`.stock.core.di.integration_tests

import `in`.stock.core.di.runtime.annotations.EntryPoint
import `in`.stock.core.di.runtime.annotations.Inject

@EntryPoint
class PrimaryConstructorEntryPoint(
	val dep: Dep
) {

	@Inject
	lateinit var b: Lazy<B>
}
