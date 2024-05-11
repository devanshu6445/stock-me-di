package `in`.stock.core.di.compiler.core

import com.google.devtools.ksp.symbol.KSNode
import `in`.stock.core.di.compiler.core.exceptions.ValidationException

abstract class ProcessingStep<Node : KSNode, Result>(
  protected open val messenger: Messenger,
  private val validator: ProcessingStepValidator<Node>
) : ProcessingStepValidator<Node> by validator {
  protected abstract fun processingStep(node: Node): Result

  fun process(node: Node): Result {
    if (validate(element = node)) {
      return processingStep(node)
    } else {
      throw ValidationException(message = "This node couldn't be validated", node = node)
    }
  }
}

interface ProcessingStepValidator<Node : KSNode> {
  fun validate(element: Node): Boolean
}