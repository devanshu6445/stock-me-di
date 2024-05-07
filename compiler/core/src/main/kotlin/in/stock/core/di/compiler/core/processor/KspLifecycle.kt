package `in`.stock.core.di.compiler.core.processor

import com.google.devtools.ksp.symbol.KSAnnotated
import `in`.stock.core.di.compiler.core.KspResolver

interface KspLifecycle {
  val state: State

  fun onCreate(resolver: KspResolver)
  fun onProcessSymbols(resolver: KspResolver): List<KSAnnotated>
  fun onFinished()

  enum class State {
    None,
    Created,
    Processing,
    Processed,
    Finished
  }
}