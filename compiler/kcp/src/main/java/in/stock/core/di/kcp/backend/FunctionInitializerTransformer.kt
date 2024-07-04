package `in`.stock.core.di.kcp.backend

import `in`.stock.core.di.kcp.utils.irBlockBody
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.jvm.ir.kClassReference
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irSetField
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.util.packageFqName
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.util.statements
import org.jetbrains.kotlin.ir.visitors.IrElementTransformer
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name

class FunctionInitializerTransformer(private val context: IrPluginContext) :
	IrElementTransformer<FunctionInitializerTransformer.Params> {

	override fun visitBlockBody(body: IrBlockBody, data: Params): IrBody {
		val superStatement = body.statements.firstOrNull { (it as? IrCall)?.superQualifierSymbol != null }

		body.statements.addAll(
			body.statements.indexOf(superStatement).let { if (data.isSuperCalledFirst) it + 1 else it },
			data.componentField.symbol.irBlockBody(context.irBuiltIns) {
				val creatorFunc = this@FunctionInitializerTransformer.context.referenceFunctions(
					callableId = CallableId(
						packageName = data.parentFunction.parentAsClass.packageFqName!!,
						callableName = Name.identifier("create")
					)
				).first()

				+irSetField(
					receiver = irGet(data.parentFunction.dispatchReceiverParameter!!),
					field = data.componentField,
					value = irCall(callee = creatorFunc, type = data.componentField.type, valueArgumentsCount = 1).apply {
						extensionReceiver = kClassReference(
							data.componentField.type
						)
						putValueArgument(0, null)
					},
				)
			}.statements
		)
		return super.visitBlockBody(body, data)
	}

	data class Params(
		val isSuperCalledFirst: Boolean,
		val componentField: IrField,
		val parentFunction: IrFunction
	)
}