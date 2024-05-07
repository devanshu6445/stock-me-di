package `in`.stock.core.di.compiler.core

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSNode
import javax.inject.Inject

abstract class Messenger(
  private val kspLogger: KSPLogger
) : KSPLogger by kspLogger {
  abstract override fun exception(e: Throwable): Nothing
  abstract fun fatalError(e: Throwable, symbol: KSNode?): Nothing
}

class MessengerImpl @Inject constructor(
  private val kspLogger: KSPLogger
) : Messenger(kspLogger) {

  override fun exception(e: Throwable): Nothing {
    kspLogger.exception(e)
    throw e
  }

  override fun fatalError(e: Throwable, symbol: KSNode?): Nothing {
    kspLogger.error(e.message.orEmpty(),symbol)
    throw e
  }
}