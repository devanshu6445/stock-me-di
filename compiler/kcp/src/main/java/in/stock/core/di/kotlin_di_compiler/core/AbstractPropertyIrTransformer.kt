package `in`.stock.core.di.kotlin_di_compiler.core

import org.jetbrains.kotlin.ir.declarations.IrProperty

interface AbstractPropertyIrTransformer : AbstractTransformerForGenerator {

  fun IrProperty.transformProperty()

  override fun visitProperty(declaration: IrProperty) {
    if (declaration.isFromPlugin(afterK2 = context.afterK2) || !declaration.shouldTransform()) {
      declaration.transformProperty()
    }
  }
}