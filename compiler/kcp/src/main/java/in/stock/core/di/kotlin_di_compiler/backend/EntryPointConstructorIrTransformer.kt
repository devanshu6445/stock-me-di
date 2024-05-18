package `in`.stock.core.di.kotlin_di_compiler.backend

import `in`.stock.core.di.kotlin_di_compiler.backend.core.AbstractConstructorIrTransformer
import `in`.stock.core.di.kotlin_di_compiler.k2.FirDeclarationGenerator
import `in`.stock.core.di.kotlin_di_compiler.utils.FqNames
import `in`.stock.core.di.kotlin_di_compiler.utils.irClass
import `in`.stock.core.di.kotlin_di_compiler.utils.irProperties
import `in`.stock.core.di.kotlin_di_compiler.utils.typesOfTypeParameters
import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.builders.irDelegatingConstructorCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irSetField
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.util.*

class EntryPointConstructorIrTransformer(
  override val context: IrPluginContext
) : AbstractConstructorIrTransformer {
  override fun generateBodyForConstructor(declaration: IrConstructor): IrBody? {
    if (!declaration.parentAsClass.annotations.hasAnnotation(FqNames.EntryPoint)) {
      error("Synthetic constructor should not be generated for non EntryPoint Class.")
    }

    val constructor = declaration.parentAsClass.primaryConstructor ?: return null
    val constructorSymbol = constructor.symbol

    val componentParameter = declaration.valueParameters.first()
    val componentClassSymbol = context.irClass(componentParameter.type)?.symbol ?: return null

    return declaration.symbol.irBlockBody {
      +irDelegatingConstructorCall(constructor).apply {
        val properties = componentClassSymbol.irProperties().associateBy {
          it.getter?.returnType ?: return@apply
        }
        for ((i, typeParameterType) in constructorSymbol.typesOfTypeParameters().withIndex()) {
          putTypeArgument(i, typeParameterType)
        }
        declaration.parentAsClass.primaryConstructor?.valueParameters?.forEachIndexed { index, irValueParameter ->
          val property = properties[irValueParameter.type]

          putValueArgument(
            index = index,
            valueArgument = IrCallImpl(
              startOffset = -1,
              endOffset = -1,
              type = irValueParameter.type,
              symbol = property?.getter?.symbol ?: return@forEachIndexed,
              typeArgumentsCount = 0,
              valueArgumentsCount = 0
            ).apply {
              dispatchReceiver = IrGetValueImpl(
                startOffset = -1,
                endOffset = -1,
                symbol = componentParameter.symbol,
              )
            }
          )
        }
      }
      +irSetField(
        receiver = irGet(declaration.parentAsClass.thisReceiver!!),
        field = declaration.parentAsClass.properties.first { it.isFromPlugin(this@EntryPointConstructorIrTransformer.context.afterK2) }.backingField!!,
        value = irGet(declaration.valueParameters.first())
      )

      for (property in declaration.parentAsClass.properties.filter { it.annotations.hasAnnotation(FqNames.Inject) }) {
        val propField =
          componentClassSymbol.owner.properties.first { it.getter?.returnType == property.getter?.returnType }

        +irSetField(
          receiver = irGet(declaration.parentAsClass.thisReceiver!!),
          field = property.backingField!!,
          value = irGet(
            type = propField.getter?.returnType!!,
            receiver = irGet(
              type = componentClassSymbol.owner.defaultType,
              variable = componentParameter,
            ),
            getterSymbol = propField.getter?.symbol!!
          )
        )
      }
    }
  }

  override val keys: List<GeneratedDeclarationKey>
    get() = listOf(FirDeclarationGenerator.Key)

}