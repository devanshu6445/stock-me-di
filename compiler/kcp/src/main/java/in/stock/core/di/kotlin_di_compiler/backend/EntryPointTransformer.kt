package `in`.stock.core.di.kotlin_di_compiler.backend

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.IrGeneratorContextBase
import org.jetbrains.kotlin.ir.builders.Scope
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid

class EntryPointTransformer(private val context: IrPluginContext) : IrElementTransformerVoid() {

  fun IrSymbol.irBlockBody(builder: IrBlockBodyBuilder.() -> Unit): IrBody {
    return IrBlockBodyBuilder(
      context = IrGeneratorContextBase(context.irBuiltIns),
      scope = Scope(this),
      startOffset = -1,
      endOffset = -1
    ).blockBody(builder)
  }
}