package `in`.stock.core.di.kotlin_di_compiler.backend

import `in`.stock.core.di.kotlin_di_compiler.core.AbstractTransformerForGenerator
import `in`.stock.core.di.kotlin_di_compiler.k2.FirDeclarationGenerator
import `in`.stock.core.di.kotlin_di_compiler.utils.FqNames
import `in`.stock.core.di.kotlin_di_compiler.utils.irClass
import `in`.stock.core.di.kotlin_di_compiler.utils.irProperties
import `in`.stock.core.di.kotlin_di_compiler.utils.typesOfTypeParameters
import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrDelegatingConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.util.primaryConstructor

class IrEntryPointTransformer(
  private val context: IrPluginContext,
) : AbstractTransformerForGenerator(context) {

  override val keys: List<GeneratedDeclarationKey>
    get() = listOf(FirDeclarationGenerator.Key)

  override fun generateBodyForFunction(
    declaration: IrSimpleFunction,
  ): IrBody {
    if (!declaration.annotations.hasAnnotation(FqNames.EntryPoint)) {
        error("Synthetic function should not be generated for non EntryPoint function.")
    }
    return context.irFactory.createBlockBody(-1, -1)
  }

  override fun generateBodyForConstructor(
    declaration: IrConstructor,
  ): IrBody? {
    if (!declaration.parentAsClass.annotations.hasAnnotation(FqNames.EntryPoint)) {
        error("Synthetic constructor should not be generated for non EntryPoint Class.")
    }

    val typeArgs = declaration.parentAsClass.primaryConstructor?.typeParameters?.size ?: -1
    val valueArgs = declaration.parentAsClass.primaryConstructor?.valueParameters?.size ?: -1

    val constructor = declaration.parentAsClass.primaryConstructor ?: return null
    val constructorSymbol = constructor.symbol

    val parentConstructorCall = IrDelegatingConstructorCallImpl(
      startOffset = -1,
      endOffset = -1,
      type = declaration.parentAsClass.defaultType,
      typeArgumentsCount = typeArgs,
      valueArgumentsCount = valueArgs,
      symbol = constructorSymbol,
    ).apply {
      val componentParameter = declaration.valueParameters.first()
      val componentClass = context.irClass(componentParameter.type)?.symbol ?: return@apply
      val properties = componentClass.irProperties().associateBy {
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

    return context.irFactory.createBlockBody(
      -1,
        -1
    ).apply {
      statements.add(parentConstructorCall)
    }
  }
}