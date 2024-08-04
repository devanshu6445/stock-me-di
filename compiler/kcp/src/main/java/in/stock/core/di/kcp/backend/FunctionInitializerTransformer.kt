package `in`.stock.core.di.kcp.backend

import `in`.stock.core.di.kcp.backend.EntryPointIrGenerator.Companion.ASSIGN_INJECTABLE_PROPERTIES
import `in`.stock.core.di.kcp.backend.core.Origin
import `in`.stock.core.di.kcp.utils.FqNames
import `in`.stock.core.di.kcp.utils.irBlockBody
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.jvm.ir.kClassReference
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irSetField
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrStatementContainer
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.types.typeOrNull
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.packageFqName
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.visitors.IrElementTransformer
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name

class FunctionInitializerTransformer(private val context: IrPluginContext) :
	IrElementTransformer<FunctionInitializerTransformer.Params> {

	override fun visitBlockBody(body: IrBlockBody, data: Params): IrBody {
		visitStatementContainer(body, data)
		return super.visitBlockBody(body, data)
	}

	private fun visitStatementContainer(container: IrStatementContainer, data: Params) {
		val superStatement = container.statements.firstOrNull { (it as? IrCall)?.superQualifierSymbol != null }

		val statementToAddIndex =
			container.statements.indexOf(superStatement).let { if (data.isSuperCalledFirst) it + 1 else it }
		container.statements.addAll(
			statementToAddIndex,
			data.componentField.symbol.irBlockBody(context.irBuiltIns) {
				val creatorFunc = this@FunctionInitializerTransformer.context.referenceFunctions(
					callableId = CallableId(
						packageName = data.parentFunction.parentAsClass.packageFqName!!,
						callableName = Name.identifier("createBoundedComponent")
					)
				)
					.first {
						(it.owner.extensionReceiverParameter?.type as IrSimpleType)
							.arguments.first().typeOrNull?.classFqName == data.componentField.type.classFqName
					}

				+irSetField(
					receiver = irGet(data.parentFunction.dispatchReceiverParameter!!),
					field = data.componentField,
					value = irCall(callee = creatorFunc, type = data.componentField.type).apply {
						extensionReceiver = kClassReference(
							data.componentField.type
						)

						putValueArgument(
							0,
							irGet(data.parentFunction.dispatchReceiverParameter!!)
						)
					},
				)

				val propertyAssignFunction = data.parentFunction.parentAsClass.declarations.filterIsInstance<IrFunction>()
					.first { it.origin == Origin && it.name.asString() == ASSIGN_INJECTABLE_PROPERTIES }

				+irCall(
					propertyAssignFunction
				).apply {
					dispatchReceiver = irGet(data.parentFunction.dispatchReceiverParameter!!)
				}
			}.statements
		)
	}

	override fun visitCall(expression: IrCall, data: Params): IrElement {
		if (expression.dispatchReceiver?.type?.getClass()?.hasAnnotation(FqNames.EntryPoint) == true) {
			println(expression)
		}
		return super.visitCall(expression, data)
	}

	data class Params(
		val isSuperCalledFirst: Boolean,
		val componentField: IrField,
		val parentFunction: IrFunction
	)
}
