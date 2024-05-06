package `in`.stock.core.di.kotlin_di_compiler.core

import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid

abstract class AbstractTransformerForGenerator(private val context: IrPluginContext) : IrElementTransformerVoid() {

    protected val irBuiltIns = context.irBuiltIns

    abstract val keys: List<GeneratedDeclarationKey>

    abstract fun generateBodyForFunction(declaration: IrSimpleFunction): IrBody?

    abstract fun generateBodyForConstructor(declaration: IrConstructor): IrBody?

    override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {

        if (!declaration.isFromPlugin(context.afterK2)) {
            return super.visitSimpleFunction(declaration)
        }

        require(declaration.body == null)
        declaration.body = generateBodyForFunction(declaration)

        return declaration
    }

    override fun visitConstructor(declaration: IrConstructor): IrStatement {

        if (!declaration.isFromPlugin(context.afterK2)) {
            return super.visitConstructor(declaration)
        }
        require(declaration.body == null)

        declaration.body = generateBodyForConstructor(declaration)
        return declaration
    }

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    private fun IrDeclaration.isFromPlugin(afterK2: Boolean): Boolean {
        val origin = origin
        return if (afterK2) {
            origin is IrDeclarationOrigin.GeneratedByPlugin && keys.any { it == origin.pluginKey }
        } else {
            (descriptor as? CallableMemberDescriptor)?.kind == CallableMemberDescriptor.Kind.SYNTHESIZED
        }
    }
}