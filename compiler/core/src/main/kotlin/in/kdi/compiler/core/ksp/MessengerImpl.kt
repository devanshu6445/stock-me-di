package `in`.kdi.compiler.core.ksp

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSNode
import `in`.kdi.compiler.core.Messenger
import javax.inject.Inject

class MessengerImpl @Inject constructor(
	private val kspLogger: KSPLogger
) : Messenger(kspLogger) {

	override fun exception(e: Throwable): Nothing {
		kspLogger.exception(e)
		throw e
	}

	override fun fatalError(e: Throwable, symbol: KSNode?): Nothing {
		kspLogger.error(e.message.orEmpty(), symbol)
		throw e
	}
}