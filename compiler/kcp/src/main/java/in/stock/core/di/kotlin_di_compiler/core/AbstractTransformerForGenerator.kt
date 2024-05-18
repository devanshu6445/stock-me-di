package `in`.stock.core.di.kotlin_di_compiler.core

import `in`.stock.core.di.kotlin_di_compiler.builders.IrBlockBodyBuilder
import `in`.stock.core.di.kotlin_di_compiler.builders.IrBuilderWithScope
import `in`.stock.core.di.kotlin_di_compiler.builders.IrGeneratorContextBase
import `in`.stock.core.di.kotlin_di_compiler.builders.Scope
import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid

interface AbstractTransformerForGenerator: IrElementVisitorVoid {

  val context: IrPluginContext

  val irBuiltIns: IrBuiltIns
    get() = context.irBuiltIns

  val keys: List<GeneratedDeclarationKey>

  fun IrDeclaration.shouldTransform(): Boolean = false

  fun IrSymbol.irBlockBody(builder: IrBlockBodyBuilder.() -> Unit): IrBody {
    return IrBlockBodyBuilder(
      context = IrGeneratorContextBase(irBuiltIns),
      scope = Scope(this),
      startOffset = -1,
      endOffset = -1
    ).blockBody(builder)
  }

  @OptIn(ObsoleteDescriptorBasedAPI::class)
  fun IrDeclaration.isFromPlugin(afterK2: Boolean): Boolean {
    val origin = origin
    return if (afterK2) {
      origin is IrDeclarationOrigin.GeneratedByPlugin && keys.any { it == origin.pluginKey }
    } else {
      (descriptor as? CallableMemberDescriptor)?.kind == CallableMemberDescriptor.Kind.SYNTHESIZED
    }
  }
}