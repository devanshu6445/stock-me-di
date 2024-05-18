package `in`.stock.core.di.kotlin_di_compiler.backend.core

import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.IrGeneratorContextBase
import org.jetbrains.kotlin.ir.builders.Scope
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid

interface AbstractTransformerForGenerator: IrElementVisitorVoid {

  val context: IrPluginContext

  val irBuiltIns: IrBuiltIns
    get() = context.irBuiltIns

  val keys: List<GeneratedDeclarationKey>

  val visitBodies: Boolean
    get() = false

  // default implementation may result in double call to isFromPlugin as other AbstractTransformers are also checking
  // isFromPlugin
  fun IrDeclaration.shouldTransform(): Boolean = isFromPlugin(context.afterK2)

  override fun visitElement(element: IrElement) {
    if (visitBodies) {
      element.acceptChildrenVoid(this)
    } else {
      when (element) {
        is IrDeclaration,
        is IrFile,
        is IrModuleFragment -> element.acceptChildrenVoid(this)
      }
    }
  }

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