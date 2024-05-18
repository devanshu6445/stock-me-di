package `in`.stock.core.di.kotlin_di_compiler

import `in`.stock.core.di.kotlin_di_compiler.builders.irGet
import `in`.stock.core.di.kotlin_di_compiler.builders.irGetField
import `in`.stock.core.di.kotlin_di_compiler.builders.irReturn
import `in`.stock.core.di.kotlin_di_compiler.builders.irSetField
import `in`.stock.core.di.kotlin_di_compiler.core.AbstractFunctionIrTransformer
import `in`.stock.core.di.kotlin_di_compiler.k2.FirDeclarationGenerator
import `in`.stock.core.di.kotlin_di_compiler.utils.FqNames
import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.isGetter
import org.jetbrains.kotlin.ir.util.isSetter

class InjectPropertyGetterSetterIrTransformer(
  override val context: IrPluginContext
) : AbstractFunctionIrTransformer {

  override val keys: List<GeneratedDeclarationKey>
    get() = listOf(FirDeclarationGenerator.Key)

  override fun generateBodyForFunction(
    declaration: IrSimpleFunction,
  ): IrBody {
    return when {
      declaration.annotations.hasAnnotation(FqNames.EntryPoint) -> {
        context.irFactory.createBlockBody(-1, -1)
      }

      declaration.isGetter -> {
        when {
          declaration.name.asString().contains("component") -> {
            generateComponentGetter(declaration)
          }

          else -> throw UnsupportedOperationException("Can't generate getter for other than component")
        }
      }

      declaration.isSetter -> {
        when {
          declaration.name.asString().contains("component") -> {
            generateComponentSetter(declaration)
          }

          else -> throw UnsupportedOperationException("Can't generate setter for other than component")
        }
      }

      else -> {
        error("Can't generate body for this type of synthetic declaration")
      }
    }
  }

  private fun generateComponentGetter(declaration: IrSimpleFunction): IrBody {
    return declaration.symbol.irBlockBody {
      +irReturn(
        irGetField(
          receiver = irGet(declaration.dispatchReceiverParameter!!),
          field = declaration.correspondingPropertySymbol?.owner?.backingField!!
        )
      )
    }
  }

  private fun generateComponentSetter(declaration: IrSimpleFunction): IrBody {
    return declaration.symbol.irBlockBody {
      +irSetField(
        irGet(declaration.dispatchReceiverParameter!!),
        declaration.correspondingPropertySymbol?.owner?.backingField!!,
        irGet(declaration.valueParameters.first())
      )
    }
  }
}