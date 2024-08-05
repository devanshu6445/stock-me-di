package `in`.stock.core.di.compiler.core

import com.google.devtools.ksp.symbol.KSNode
import `in`.stock.core.di.compiler.core.exceptions.ValidationException

abstract class XProcessingStep<Node : KSNode, Result, Data>(
  private val validator: ProcessingStepValidator<Node>
) : ProcessingStepValidator<Node> by validator {
	protected abstract fun step(node: Node, data: Data): Result

	fun process(node: Node, data: Data): Result {
    if (validate(element = node)) {
			return step(node, data)
    } else {
      throw ValidationException(message = "This node couldn't be validated", node = node)
    }
  }
}

interface ProcessingStepValidator<Node : KSNode> {
	// todo add a mechanism to pass message for as to why this node was not validated
  fun validate(element: Node): Boolean
}