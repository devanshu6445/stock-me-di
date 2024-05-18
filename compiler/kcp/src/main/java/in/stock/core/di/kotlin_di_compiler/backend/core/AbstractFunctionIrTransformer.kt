package `in`.stock.core.di.kotlin_di_compiler.backend.core

import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrBody

interface AbstractFunctionIrTransformer: AbstractTransformerForGenerator {

  fun generateBodyForFunction(declaration: IrSimpleFunction): IrBody?

  override fun visitSimpleFunction(declaration: IrSimpleFunction) {
    if (!declaration.isFromPlugin(context.afterK2) || !declaration.shouldTransform()) {
      return super.visitSimpleFunction(declaration)
    }

    require(declaration.body == null)
    declaration.body = generateBodyForFunction(declaration)
  }
}