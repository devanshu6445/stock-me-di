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
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid

class EntryPointIrGenerator(
  override val context: IrPluginContext,
  private val entryPointConstructorIrTransformer: AbstractConstructorIrTransformer,
  private val injectPropertyIrTransformer: AbstractPropertyIrTransformer,
  private val injectPropertyGetterSetterIrTransformer: AbstractFunctionIrTransformer,
  private val injectIrPropertyGenerator: InjectIrPropertyGenerator
) : IrElementTransformerVoid() {

  override val keys: List<GeneratedDeclarationKey>
    get() = listOf(FirDeclarationGenerator.Key)

  override fun IrProperty.transformProperty() = with(injectPropertyIrTransformer) {
    transformProperty()
  }

  override fun generateBodyForConstructor(declaration: IrConstructor): IrBody? =
    entryPointConstructorIrTransformer.generateBodyForConstructor(declaration)

  override fun generateBodyForFunction(declaration: IrSimpleFunction): IrBody? =
    injectPropertyGetterSetterIrTransformer.generateBodyForFunction(declaration)

//  override fun visitClass(declaration: IrClass) {
//    injectIrPropertyGenerator.visitClass(declaration)
//    super<AbstractFunctionIrTransformer>.visitClass(declaration)
//  }
}

class A(val d: B) {
  private val b: B = B()
}

class B