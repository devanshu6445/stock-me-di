package `in`.stock.core.di.compiler.core

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSNode

abstract class Messenger(
  private val kspLogger: KSPLogger
) : KSPLogger by kspLogger {
  abstract override fun exception(e: Throwable): Nothing
  abstract fun fatalError(e: Throwable, symbol: KSNode?): Nothing
}