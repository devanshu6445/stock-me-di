package `in`.stock.core.di.kotlin_di_compiler.backend.core

import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.expressions.IrBody

interface AbstractConstructorIrTransformer : AbstractTransformerForGenerator {

  fun generateBodyForConstructor(declaration: IrConstructor): IrBody?

  override fun visitConstructor(declaration: IrConstructor) {
    if (!declaration.isFromPlugin(context.afterK2) || !declaration.shouldTransform()) {
      return super.visitConstructor(declaration)
    }
    require(declaration.body == null)

    declaration.body = generateBodyForConstructor(declaration)
  }

}