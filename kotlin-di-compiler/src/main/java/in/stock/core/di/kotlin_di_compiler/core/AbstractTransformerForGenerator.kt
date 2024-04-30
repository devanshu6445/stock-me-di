package `in`.stock.core.di.kotlin_di_compiler.core

import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid

abstract class AbstractTransformerForGenerator : IrElementTransformerVoid() {

    abstract fun interestedIn(key: GeneratedDeclarationKey?): Boolean
    abstract fun generateBodyForFunction(declaration: IrSimpleFunction,key: GeneratedDeclarationKey?): IrBody

    abstract fun generateBodyForConstructor(declaration: IrConstructor,key: GeneratedDeclarationKey?): IrBody

    override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
        val origin = declaration.origin
        if (origin !is IrDeclarationOrigin.GeneratedByPlugin || !interestedIn(origin.pluginKey)) {
            return super.visitSimpleFunction(declaration)
        }
        require(declaration.body == null)
        declaration.body = generateBodyForFunction(declaration, origin.pluginKey)

        return declaration
    }

    override fun visitConstructor(declaration: IrConstructor): IrStatement {
        val origin = declaration.origin

        if (origin !is IrDeclarationOrigin.GeneratedByPlugin || !interestedIn(origin.pluginKey)) {
            return super.visitConstructor(declaration)
        }
        require(declaration.body == null)
        declaration.body = generateBodyForConstructor(declaration,origin.pluginKey)
        return declaration
    }
}