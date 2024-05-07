package `in`.stock.core.di.kotlin_di_compiler.utils

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.types.impl.IrStarProjectionImpl
import org.jetbrains.kotlin.ir.util.constructedClass
import org.jetbrains.kotlin.ir.util.properties

fun IrType.replaceArgumentsWithStarProjections(): IrType =
  when (this) {
    is IrSimpleType -> IrSimpleTypeImpl(
      classifier,
      isMarkedNullable(),
      List(arguments.size) { IrStarProjectionImpl },
      annotations,
      abbreviation
    )

    else -> this
  }

fun IrPluginContext.irClass(irType: IrType): IrClass? {
  return irType.getClass()
}

fun IrClassSymbol.irProperties(): Sequence<IrProperty> {
  return owner.properties
}

fun IrConstructorSymbol.typesOfTypeParameters(): List<IrType> {
  val allParameters = owner.constructedClass.typeParameters + owner.typeParameters
  return allParameters.map { it.defaultType }
}