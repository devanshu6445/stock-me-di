package `in`.stock.core.di.runtime

import `in`.stock.core.di.runtime.internal.ComponentGenerator
import `in`.stock.core.di.runtime.internal.GeneratedComponent
import kotlin.reflect.KClass
import kotlin.reflect.cast

object RetrieverGetter {

	fun <T : Any> get(obj: Any?, retriever: KClass<T>): T {
		return when (obj) {
			is ComponentGenerator<*> -> get(obj.generateComponent(), retriever)
			is GeneratedComponent -> retriever.cast(obj)
			else -> error("This retriever is not installed in entry point($obj)")
		}
	}
}