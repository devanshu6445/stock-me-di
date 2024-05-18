package `in`.stock.core.di.kotlin_di_compiler.backend.core

import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid

interface AbstractPropertyIrTransformer : AbstractTransformerForGenerator {

  fun IrProperty.transformProperty()

  override fun visitProperty(declaration: IrProperty) {
    if (!declaration.isFromPlugin(afterK2 = context.afterK2) || !declaration.shouldTransform()) {
      return
    }
    declaration.transformProperty()
    declaration.acceptChildrenVoid(this)
  }
}