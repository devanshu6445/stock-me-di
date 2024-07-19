package `in`.stock.core.di.kcp.backend

import `in`.stock.core.di.kcp.backend.core.AbstractTransformerForGenerator
import `in`.stock.core.di.kcp.backend.core.Origin
import `in`.stock.core.di.kcp.k2.FirDeclarationGenerator
import `in`.stock.core.di.kcp.utils.*
import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.backend.js.utils.valueArguments
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.addBackingField
import org.jetbrains.kotlin.ir.builders.declarations.addFunction
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.typeOrFail
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

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

					else -> {
						throw UnsupportedOperationException("Can't generate getter for other than component")
					}
				}
			}

			declaration.isSetter -> {
				when {
					declaration.name.asString().contains("component") -> {
						generateComponentSetter(declaration)
					}

					else -> {
						throw UnsupportedOperationException("Can't generate setter for other than component")
					}
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
			error("Synthetic constructor body should not be generated for non EntryPoint Class.")
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
			componentAssignment(declaration)

			// assign all the injectable properties
			propertyAssignment(
				declaration,
				componentClassSymbol,
				componentGetter = declaration.parentAsClass.properties.first { it.name == Name.identifier("component") }.getter!!
			)
		}
	}

	private fun IrBlockBodyBuilder.componentAssignment(declaration: IrFunction) {
		+irSetField(
			receiver = irGet(declaration.parentAsClass.thisReceiver!!),
			field = declaration.parentAsClass.properties.first {
				it.isFromPlugin(this@EntryPointIrGenerator.context.afterK2)
			}.backingField!!,
			value = irGet(declaration.valueParameters.first())
		)
	}

	private fun IrBlockBodyBuilder.propertyAssignment(
		declaration: IrFunction,
		componentClassSymbol: IrClassSymbol,
		componentGetter: IrFunction,
		receiver: IrValueParameter = declaration.parentAsClass.thisReceiver!!
	) {
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
					receiver = irGet(receiver),
					field = property.backingField!!,
					value = irCall(
						lazyPropertyCreatorFunction
					).apply {
						putValueArgument(
							0,
							this@EntryPointIrGenerator.context.irLambdaExpression(
								startOffset,
								endOffset,
								propertyTypeWithoutLazy!!
							) {
								it.body = it.symbol.irBlockBody {
									+irReturn(
										irGet(
											type = propField.getter?.returnType!!,
											receiver = irGet(
												type = componentGetter.returnType,
												receiver = irGet(receiver),
												getterSymbol = componentGetter.symbol
											),
											getterSymbol = propField.getter?.symbol!!
										)
									)
								}
							}
						)
					}
				)
			} else {
				// set the values of all the @Inject annotated fields
				+irSetField(
					receiver = irGet(receiver),
					field = if (property.isFakeOverride) {
						property.overriddenSymbols
							.first().owner.backingField!!
					} else {
						property.backingField!!
					},
					value = irGet(
						type = propField.getter?.returnType!!,
						receiver = irGet(
							type = componentClassSymbol.owner.defaultType,
							receiver = irGet(receiver),
							getterSymbol = componentGetter.symbol
						),
						getterSymbol = propField.getter?.symbol!!
					)
				)
			}
		}
	}

	override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
		return try {
			// check if class is to be injected
			val injectableClass = declaration.parentAsClass.let { clazz ->
				if (clazz.hasAnnotation(FqNames.EntryPoint)) {
					clazz
				} else {
					return super.visitSimpleFunction(declaration)
				}
			}

			// get all the arguments
			val entryPointAnnotationParams =
				injectableClass.getAnnotation(FqNames.EntryPoint)?.valueArguments?.filterIsInstance<IrConst<*>>()!!

			// get the arguments in order they were defined in the source
			val initializer = entryPointAnnotationParams.firstOrNull()?.value?.toString() ?: "constructor"
			val isSuperCalledFirst = entryPointAnnotationParams.getOrNull(1)?.value?.toString()?.toBoolean() ?: false

			// transform this function by InitializerTransformer if needed
			if (declaration.name.asString() == initializer) {
				val componentProperty = visitProperty(
					declaration.parentAsClass.properties.first {
						it.isFromPlugin(this@EntryPointIrGenerator.context.afterK2) && it.name.asString() == "component"
					}
				) as IrProperty

				generatePropertyAssigningFunction(
					declaration = declaration.parentAsClass
				)

				declaration.transformChildren(
					FunctionInitializerTransformer(
						context = context
					),
					FunctionInitializerTransformer.Params(
						isSuperCalledFirst = isSuperCalledFirst,
						componentField = componentProperty.backingField!!,
						parentFunction = declaration
					)
				)
			}

			// todo check if returning this without calling super transformation is safe
			super.visitSimpleFunction(declaration)
		} catch (e: Exception) {
			super.visitSimpleFunction(declaration)
		}
	}

	private fun generatePropertyAssigningFunction(declaration: IrClass): IrFunction {
		val existingFunction = declaration.functions.firstOrNull { it.name.asString() == "assignInjectableProperties" }
		if (existingFunction != null) {
			return existingFunction
		}

		val generatedFunction = declaration.addFunction(
			name = ASSIGN_INJECTABLE_PROPERTIES,
			returnType = context.irBuiltIns.unitType,
			visibility = DescriptorVisibilities.PRIVATE,
			origin = Origin
		).apply {
			val componentType = declaration.properties.first { it.name == Name.identifier("component") }

			body = symbol.irBlockBody {
				propertyAssignment(
					declaration = this@apply,
					componentClassSymbol = this@EntryPointIrGenerator.context.irClass(
						componentType.getter?.returnType!!
					)?.symbol!!,
					componentGetter = componentType.getter!!,
					receiver = dispatchReceiverParameter!!
				)
			}
		}

		return generatedFunction
	}

	companion object {
		const val ASSIGN_INJECTABLE_PROPERTIES = "assignInjectableProperties"
	}
}