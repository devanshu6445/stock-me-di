package `in`.stock.core.di.compiler.core.processor

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSNode
import com.squareup.kotlinpoet.ClassName
import `in`.stock.core.di.compiler.core.KspResolver

interface KspLifecycle {
  val state: State

  fun onCreate(resolver: KspResolver)
  fun processSymbol(resolver: KspResolver, symbol: KSAnnotated, annotation: ClassName): Boolean
  fun roundFinished()
  fun onFinished()

  enum class State {
    None,
    Created,
    Processing,
    Processed,
    Finished
  }
}