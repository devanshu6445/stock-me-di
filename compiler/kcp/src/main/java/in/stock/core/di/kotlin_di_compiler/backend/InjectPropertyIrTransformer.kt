package `in`.stock.core.di.kotlin_di_compiler.backend

import `in`.stock.core.di.kotlin_di_compiler.backend.core.AbstractPropertyIrTransformer
import `in`.stock.core.di.kotlin_di_compiler.builders.declarations.addBackingField
import `in`.stock.core.di.kotlin_di_compiler.k2.FirDeclarationGenerator
import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrProperty

class InjectPropertyIrTransformer(
  override val context: IrPluginContext
) : AbstractPropertyIrTransformer {
  override fun IrProperty.transformProperty() {
    addBackingField {
      type = getter?.returnType!!
    }
  }

  override val keys: List<GeneratedDeclarationKey>
    get() = listOf(FirDeclarationGenerator.Key)
}