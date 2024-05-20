package `in`.stock.core.di.kotlin_di_compiler.utils

import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.symbols.IrClassifierSymbol
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.SimpleTypeNullability
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.types.Variance

fun IrClassifierSymbol.makeTypeProjection(
  type: IrType,
  variance: Variance,
  nullability: SimpleTypeNullability = SimpleTypeNullability.NOT_SPECIFIED,
  annotation: List<IrConstructorCall> = emptyList()
): IrSimpleType {
  return IrSimpleTypeImpl(
    this,
    nullability,
    arguments = listOf(
      org.jetbrains.kotlin.ir.types.impl.makeTypeProjection(
        type = type,
        variance = variance
      )
    ),
    annotations = annotation
  )
}