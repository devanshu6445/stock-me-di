package `in`.stock.core.di.kotlin_di_compiler.backend

import `in`.stock.core.di.kotlin_di_compiler.backend.core.AbstractConstructorIrTransformer
import `in`.stock.core.di.kotlin_di_compiler.backend.core.AbstractFunctionIrTransformer
import `in`.stock.core.di.kotlin_di_compiler.backend.core.AbstractPropertyIrTransformer
import `in`.stock.core.di.kotlin_di_compiler.k2.FirDeclarationGenerator
import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrBody

class EntryPointIrGenerator(
  override val context: IrPluginContext,
  private val entryPointConstructorIrTransformer: AbstractConstructorIrTransformer,
  private val injectPropertyIrTransformer: AbstractPropertyIrTransformer,
  private val injectPropertyGetterSetterIrTransformer: AbstractFunctionIrTransformer
) :
  AbstractConstructorIrTransformer, AbstractPropertyIrTransformer, AbstractFunctionIrTransformer {
  override fun IrProperty.transformProperty() = with(injectPropertyIrTransformer) {
    transformProperty()
  }

  override fun generateBodyForConstructor(declaration: IrConstructor): IrBody? =
    entryPointConstructorIrTransformer.generateBodyForConstructor(declaration)

  override val keys: List<GeneratedDeclarationKey>
    get() = listOf(FirDeclarationGenerator.Key)

  override fun generateBodyForFunction(declaration: IrSimpleFunction): IrBody? =
    injectPropertyGetterSetterIrTransformer.generateBodyForFunction(declaration)
}