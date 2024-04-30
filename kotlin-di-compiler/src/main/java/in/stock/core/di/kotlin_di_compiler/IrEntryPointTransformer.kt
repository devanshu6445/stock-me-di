package `in`.stock.core.di.kotlin_di_compiler

import `in`.stock.core.di.kotlin_di_compiler.core.AbstractTransformerForGenerator
import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrBody

class IrEntryPointTransformer(
    private val context: IrPluginContext,
) : AbstractTransformerForGenerator() {

    override fun interestedIn(key: GeneratedDeclarationKey?): Boolean {
        return key == FirDeclarationGenerator.Key
    }

    override fun generateBodyForFunction(
        declaration: IrSimpleFunction,
        key: GeneratedDeclarationKey?
    ): IrBody {
        return context.irFactory.createBlockBody(-1,-1)
    }

    override fun generateBodyForConstructor(
        declaration: IrConstructor,
        key: GeneratedDeclarationKey?
    ): IrBody {
        return context.irFactory.createBlockBody(-1,-1)
    }
}