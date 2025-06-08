package `in`.kdi.integration_tests

import `in`.kdi.runtime.annotations.EntryPoint
import `in`.kdi.runtime.annotations.Inject

@EntryPoint
class PrimaryConstructorEntryPoint(
	val dep: Dep
) {

	@Inject
	lateinit var b: Lazy<B>
}