package `in`.stock.core.di.kcp.backend.core

import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.IrGeneratorContextBase
import org.jetbrains.kotlin.ir.builders.Scope
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.name.FqName

abstract class AbstractTransformerForGenerator : IrElementTransformerVoid() {

  abstract val context: IrPluginContext

  val irBuiltIns: IrBuiltIns
    get() = context.irBuiltIns

  abstract val keys: List<GeneratedDeclarationKey>

  val visitBodies: Boolean
    get() = false

  // default implementation may result in double call to isFromPlugin as other AbstractTransformers are also checking
  // isFromPlugin
  protected fun IrDeclaration.shouldTransform(): Boolean = isFromPlugin(context.afterK2)

  abstract fun generateBodyForFunction(declaration: IrSimpleFunction): IrBody?

  override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
    if (!declaration.isFromPlugin(context.afterK2) || !declaration.shouldTransform()) {
      return super.visitSimpleFunction(declaration)
    }

		// require(declaration.body == null)
    if (declaration.body == null) {
      declaration.body = generateBodyForFunction(declaration)
    }
    return super.visitSimpleFunction(declaration)
  }

  abstract fun IrProperty.transformProperty()

  override fun visitProperty(declaration: IrProperty): IrStatement {
    if (!declaration.isFromPlugin(afterK2 = context.afterK2) || !declaration.shouldTransform()) {
      return super.visitProperty(declaration)
    }
    declaration.transformProperty()
    return super.visitProperty(declaration)
  }

  abstract fun generateBodyForConstructor(declaration: IrConstructor): IrBody?

  override fun visitConstructor(declaration: IrConstructor): IrStatement {
    if (!declaration.isFromPlugin(context.afterK2) || !declaration.shouldTransform()) {
      return super.visitConstructor(declaration)
    }
    require(declaration.body == null)

    declaration.body = generateBodyForConstructor(declaration)
    return super.visitConstructor(declaration)
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
			(descriptor as? CallableMemberDescriptor)?.kind == CallableMemberDescriptor.Kind.SYNTHESIZED && hasAnnotation(
				FqName("in.stock.core.di.runtime.annotations.internals.GeneratedByPlugin")
			)
    }
  }
}