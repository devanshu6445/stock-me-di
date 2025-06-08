package `in`.kdi.runtime

import `in`.kdi.runtime.internal.GeneratedComponent
import kotlin.reflect.KClass
import kotlin.reflect.cast

object RetrieverGetter {

	fun <T : Any> get(obj: Any?, retriever: KClass<T>): T {
		return when (obj) {
			is `in`.kdi.runtime.internal.ComponentGenerator<*> -> `in`.kdi.runtime.RetrieverGetter.get(
				obj.generateComponent(),
				retriever
			)
			is GeneratedComponent -> retriever.cast(obj)
			else -> error("This retriever is not installed in entry point($obj)")
		}
	}
}