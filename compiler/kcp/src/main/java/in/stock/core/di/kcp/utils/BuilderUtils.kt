package `in`.stock.core.di.kcp.utils

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.IrFunctionBuilder
import org.jetbrains.kotlin.ir.builders.declarations.addSetter
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionExpressionImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames

fun IrProperty.addDefaultSetter(
  parentClass: IrClass,
  irBuiltIns: IrBuiltIns,
  builder: IrFunctionBuilder.() -> Unit = {}
) {
  addSetter(builder = builder)
    .apply {
      addValueParameter {
        type = returnType
        name = Name.identifier("value")
      }
      body = symbol.irBlockBody(irBuiltIns) {
        +irSetField(
          irGet(parentClass.thisReceiver!!),
          correspondingPropertySymbol?.owner?.backingField!!,
          irGet(valueParameters.first())
        )
      }
    }
}

fun IrSymbol.irBlockBody(irBuiltIns: IrBuiltIns, builder: IrBlockBodyBuilder.() -> Unit): IrBlockBody {
  return IrBlockBodyBuilder(
    context = IrGeneratorContextBase(irBuiltIns),
    scope = Scope(this),
    startOffset = -1,
    endOffset = -1
  ).blockBody(builder)
}

fun IrPluginContext.irLambdaExpression(
  startOffset: Int,
  endOffset: Int,
  returnType: IrType,
  body: (IrSimpleFunction) -> Unit
): IrExpression {
  val function = irFactory.buildFun {
    this.startOffset = SYNTHETIC_OFFSET
    this.endOffset = SYNTHETIC_OFFSET
    this.returnType = returnType
    origin = IrDeclarationOrigin.LOCAL_FUNCTION_FOR_LAMBDA
    name = SpecialNames.ANONYMOUS
    visibility = DescriptorVisibilities.LOCAL
  }.also(body)

  return IrFunctionExpressionImpl(
    startOffset = startOffset,
    endOffset = endOffset,
    type = this.function(function.valueParameters.size).typeWith(
      function.valueParameters.map { it.type } + listOf(function.returnType)
    ),
    origin = IrStatementOrigin.LAMBDA,
    function = function
  )
}

fun IrPluginContext.function(arity: Int): IrClassSymbol =
  referenceClass(ClassId(FqName("kotlin"), Name.identifier("Function$arity")))!!
