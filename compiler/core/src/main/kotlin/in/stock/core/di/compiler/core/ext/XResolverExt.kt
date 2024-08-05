package `in`.stock.core.di.compiler.core.ext

import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSVisitor
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.ClassName
import `in`.stock.core.di.compiler.core.XResolver
import kotlin.reflect.KClass

inline fun <reified T : KSNode> XResolver.getSymbols(cls: KClass<*>) =
	this.getSymbolsWithAnnotation(cls.qualifiedName.orEmpty())
		.filterIsInstance<T>()
		.filter(KSNode::validate)

fun XResolver.getSymbols(className: ClassName) =
	this.getSymbolsWithAnnotation(className.canonicalName)
		.filter(KSNode::validate)

inline fun <reified T : KSNode> XResolver.getSymbols(
	cls: KClass<*>,
	validator: KSVisitor<Unit, Unit>,
) =
	this.getSymbolsWithAnnotation(cls.qualifiedName.orEmpty())
		.filterIsInstance<T>()
		.filter(KSNode::validate)
		.apply {
			forEach {
				it.accept(validator, Unit)
			}
		}