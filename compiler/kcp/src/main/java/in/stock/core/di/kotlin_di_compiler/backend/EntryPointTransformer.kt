package `in`.stock.core.di.kotlin_di_compiler.backend

import `in`.stock.core.di.kotlin_di_compiler.utils.FqNames
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.IrGeneratorContextBase
import org.jetbrains.kotlin.ir.builders.Scope
import org.jetbrains.kotlin.ir.builders.declarations.addProperty
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.properties
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

class EntryPointTransformer(private val context: IrPluginContext) : IrElementTransformerVoid() {

  fun IrSymbol.irBlockBody(builder: IrBlockBodyBuilder.() -> Unit): IrBody {
    return IrBlockBodyBuilder(
      context = IrGeneratorContextBase(context.irBuiltIns),
      scope = Scope(this),
      startOffset = -1,
      endOffset = -1
    ).blockBody(builder)
  }

  override fun visitClass(declaration: IrClass): IrStatement {
    if (!declaration.hasAnnotation(FqNames.EntryPoint))
      return super.visitClass(declaration)

    val properties = declaration.properties.filter { it.annotations.hasAnnotation(FqNames.Inject) }.toList()

    val lazyType = context.referenceClass(ClassId(FqName("kotlin"), Name.identifier("Lazy")))!!

    val lazyPropertyCreatorFunction = context.referenceFunctions(
      callableId = CallableId(
        packageName = FqName("kotlin"),
        callableName = Name.identifier("lazy")
      )
    ).first()

    for (props in properties) {
      declaration.addProperty {
        name = Name.identifier(props.name.asString() + "Delegate")
        modality = Modality.FINAL
      }
    }

    return super.visitClass(declaration)
  }
}