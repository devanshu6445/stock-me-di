package `in`.stock.core.di.compiler.ksp.ext

import com.google.devtools.ksp.symbol.KSAnnotated
import `in`.stock.core.di.compiler.ksp.utils.getAnnotation
import kotlin.reflect.KClass

fun <T> KSAnnotated.getArrayArgument(annotation: KClass<*>, name: String): List<T> = getArgument<ArrayList<T>?>(
	annotation = annotation,
	name = name
) ?: emptyList()

@Suppress("UNCHECKED_CAST")
fun <T> KSAnnotated.getArgument(annotation: KClass<*>, name: String) =
	getAnnotation(annotation)?.arguments?.firstOrNull { it.name?.asString() == name }
		?.value as T
