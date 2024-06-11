package `in`.stock.core.di.kotlin_di_compiler.backend

import `in`.stock.core.di.kotlin_di_compiler.backend.core.AbstractTransformerForGenerator
import `in`.stock.core.di.kotlin_di_compiler.k2.FirDeclarationGenerator
import `in`.stock.core.di.kotlin_di_compiler.utils.*
import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.addBackingField
import org.jetbrains.kotlin.ir.builders.declarations.addDefaultGetter
import org.jetbrains.kotlin.ir.builders.declarations.addProperty
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.typeOrFail
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.Variance

// todo refactor it using builder pattern for generation
class EntryPointIrGenerator(
  override val context: IrPluginContext,
) : AbstractTransformerForGenerator() {

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

      declaration.name.asString().contains("Builder") -> {
        val builderType = declaration.parentAsClass.properties.first { it.getter?.returnType == declaration.returnType }

        declaration.symbol.irBlockBody {
          +irReturn(
            irGet(
              type = builderType.getter?.returnType!!,
              receiver = irGet(declaration.parentAsClass.thisReceiver!!),
              getterSymbol = builderType.getter?.symbol!!
            )
          )
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

  override fun IrProperty.transformProperty() {
    if (backingField == null) {
      addBackingField {
        type = getter?.returnType!!
      }
    }
  }

  override fun generateBodyForConstructor(declaration: IrConstructor): IrBody? {
    if (!declaration.parentAsClass.annotations.hasAnnotation(FqNames.EntryPoint)) {
      error("Synthetic constructor should not be generated for non EntryPoint Class.")
    }

    val constructor = declaration.parentAsClass.primaryConstructor ?: return null
    val constructorSymbol = constructor.symbol

    val componentParameter = declaration.valueParameters.first()
    val componentClassSymbol = context.irClass(componentParameter.type)?.symbol ?: return null

    return declaration.symbol.irBlockBody {
      // call the primary constructor of this class
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
      // set the component field with the component property of the secondary constructor
      +irSetField(
        receiver = irGet(declaration.parentAsClass.thisReceiver!!),
        field = declaration.parentAsClass.properties.first { it.isFromPlugin(this@EntryPointIrGenerator.context.afterK2) }.backingField!!,
        value = irGet(declaration.valueParameters.first())
      )

      for (property in declaration.parentAsClass.properties.filter { it.annotations.hasAnnotation(FqNames.Inject) }) {
        val propertyType = property.getter?.returnType

        val isLazyType = propertyType?.classFqName?.asString() == "kotlin.Lazy"

        val propertyTypeWithoutLazy =
          if (isLazyType) (propertyType as IrSimpleType).arguments.first().typeOrFail else propertyType

        val propField =
          componentClassSymbol.owner.properties.first { it.getter?.returnType == propertyTypeWithoutLazy }

        if (isLazyType) {
          val lazyPropertyCreatorFunction = this@EntryPointIrGenerator.context.referenceFunctions(
            callableId = CallableId(
              packageName = FqName("kotlin"),
              callableName = Name.identifier("lazy")
            )
          ).first()

          +irSetField(
            receiver = irGet(declaration.parentAsClass.thisReceiver!!),
            field = property.backingField!!,
            value = irCall(
              lazyPropertyCreatorFunction
            ).apply {
              putValueArgument(0, this@EntryPointIrGenerator.context.irLambdaExpression(
                startOffset,
                endOffset,
                propertyTypeWithoutLazy!!
              ) {
                it.body = it.symbol.irBlockBody {
                  +irReturn(
                    irGet(
                      type = propField.getter?.returnType!!,
                      receiver = irGet(componentParameter),
                      getterSymbol = propField.getter?.symbol!!
                    )
                  )
                }
              })
            }
          )
        } else {
          // set the values of all the @Inject annotated fields
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
  }

  override fun visitClass(declaration: IrClass): IrStatement {
    if (!declaration.hasAnnotation(FqNames.EntryPoint))
      return super.visitClass(declaration)

    val properties = declaration.properties.filter { it.annotations.hasAnnotation(FqNames.Inject) }.toList()

    val lazyType = context.referenceClass(ClassId(FqName("kotlin"), Name.identifier("Lazy")))!!

    for (props in properties.filter { it.hasAnnotation(FqNames.Inject) }) {
      declaration.addProperty {
        name = Name.identifier(props.name.asString() + "Delegate")
        modality = Modality.FINAL
      }.apply {
        addBackingField {
          type = lazyType.makeTypeProjection(
            type = props.getter?.returnType!!,
            variance = Variance.INVARIANT
          )
        }.apply {
          val lazyPropertyCreatorFunction = context.referenceFunctions(
            callableId = CallableId(
              packageName = FqName("kotlin"),
              callableName = Name.identifier("lazy")
            )
          ).first()

          initializer = context.irFactory.createExpressionBody(
            startOffset = -1,
            endOffset = -1,
            expression = IrCallImpl(
              startOffset = -1,
              endOffset = -1,
              type = lazyPropertyCreatorFunction.owner.returnType,
              symbol = lazyPropertyCreatorFunction,
              typeArgumentsCount = lazyPropertyCreatorFunction.owner.typeParameters.size,
              valueArgumentsCount = lazyPropertyCreatorFunction.owner.valueParameters.size
            ).apply {

              putTypeArgument(
                0,
                type
              )

              putValueArgument(
                0,
                context.irLambdaExpression(
                  startOffset,
                  endOffset,
                  props.getter?.returnType!!
                ) {
                  it.body = it.symbol.irBlockBody {
                    +irReturn(
                      irGetField(
                        receiver = irGet(declaration.thisReceiver!!),
                        field = props.backingField!!
                      )
                    )
                  }
                }
              )
            }
          )
        }
        addDefaultGetter(declaration, irBuiltIns)
      }
    }

    return super.visitClass(declaration)
  }

  override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
    declaration.correspondingPropertySymbol?.let { property ->
      if (property.owner.hasAnnotation(FqNames.Inject)) {
        when {
          declaration.isGetter -> {
            declaration.body = declaration.symbol.irBlockBody {
              val component = declaration.parentAsClass.properties.first { it.name.asString().contains("component") }
              declaration.body = declaration.symbol.irBlockBody {
                +irReturn(
                  irGet(
                    type = declaration.returnType,
                    receiver = irGet(declaration.parentAsClass.thisReceiver!!),
                    getterSymbol = component.getter?.symbol!!
                  )
                )
              }
            }
          }

          declaration.isSetter -> {
            declaration.body = declaration.symbol.irBlockBody {

            }
          }
        }
      }
    }
    return super.visitSimpleFunction(declaration)
  }

  override fun visitProperty(declaration: IrProperty): IrStatement {
    if (declaration.hasAnnotation(FqNames.Inject)) {
      declaration.getter?.body = null
      declaration.setter?.body = null
    }
    return super.visitProperty(declaration)
  }
}