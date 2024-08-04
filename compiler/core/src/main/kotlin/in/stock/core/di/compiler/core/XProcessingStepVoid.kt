package `in`.stock.core.di.compiler.core

import com.google.devtools.ksp.symbol.KSNode

abstract class XProcessingStepVoid<Node : KSNode, Result>(
	private val validator: ProcessingStepValidator<Node>
) : XProcessingStep<Node, Result, Unit>(
	validator
) {

	protected abstract fun step(node: Node): Result

	final override fun step(node: Node, data: Unit): Result = step(node)

	fun process(node: Node): Result {
		return super.process(
			node = node,
			data = Unit
		)
	}
}
