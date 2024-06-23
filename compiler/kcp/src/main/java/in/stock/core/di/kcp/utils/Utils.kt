package `in`.stock.core.di.kcp.utils

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.expressions.IrSetField
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.types.impl.IrStarProjectionImpl
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.utils.filterIsInstanceAnd

fun IrType.replaceArgumentsWithStarProjections(): IrType =
  when (this) {
    is IrSimpleType -> {
      IrSimpleTypeImpl(
        classifier,
        isMarkedNullable(),
        List(arguments.size) { IrStarProjectionImpl },
        annotations,
        abbreviation
      )
    }

    else -> {
      this
    }
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

fun IrSimpleFunction.getSetterField(): IrField? {
  if (!isSetter) return null
  val statement = (body?.statements?.singleOrNull() as? IrReturn)?.value as? IrSetField ?: return null
  return statement.symbol.owner
}

// This declaration accesses IrDeclarationContainer.declarations, which is marked with this opt-in
fun IrClass.getPropertyGetter(name: String): IrSimpleFunctionSymbol? =
  getPropertyDeclaration(name)?.getter?.symbol
		?: getSimpleFunction("<get-$name>").also {
			assert(it?.owner?.correspondingPropertySymbol?.owner?.name?.asString() == name)
		}

private fun IrClass.getPropertyDeclaration(name: String): IrProperty? {
  val properties = declarations.filterIsInstanceAnd<IrProperty> { it.name.asString() == name }
  if (properties.size > 1) {
    error(
      "More than one property with name $name in class $fqNameWhenAvailable:\n" +
              properties.joinToString("\n", transform = IrProperty::render)
    )
  }
  return properties.firstOrNull()
}