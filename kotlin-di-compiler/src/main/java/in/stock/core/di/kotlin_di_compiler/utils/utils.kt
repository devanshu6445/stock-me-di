package `in`.stock.core.di.kotlin_di_compiler.utils

import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.types.impl.IrStarProjectionImpl
import org.jetbrains.kotlin.ir.types.isMarkedNullable

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