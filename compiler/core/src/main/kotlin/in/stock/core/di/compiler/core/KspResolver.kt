package `in`.stock.core.di.compiler.core

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSVisitor
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.ClassName
import kotlin.reflect.KClass

class KspResolver(
  private val delegate: Resolver
) : Resolver by delegate {

  inline fun <reified T : KSNode> Resolver.getSymbols(cls: KClass<*>) =
    this.getSymbolsWithAnnotation(cls.qualifiedName.orEmpty())
      .filterIsInstance<T>()
      .filter(KSNode::validate)

  private fun Resolver.getSymbols(className: ClassName) =
    this.getSymbolsWithAnnotation(className.canonicalName)
      .filter(KSNode::validate)

  inline fun <reified T : KSNode> Resolver.getSymbols(
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
}