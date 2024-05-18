package `in`.stock.core.di.kotlin_di_compiler.backend

import `in`.stock.core.di.kotlin_di_compiler.backend.core.AbstractTransformerForGenerator
import `in`.stock.core.di.kotlin_di_compiler.k2.FirDeclarationGenerator
import `in`.stock.core.di.kotlin_di_compiler.utils.FqNames
import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.builders.declarations.addBackingField
import org.jetbrains.kotlin.ir.builders.declarations.addProperty
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.types.SimpleTypeNullability
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.types.impl.makeTypeProjection
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.properties
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.Variance

class InjectIrPropertyGenerator(
  override val context: IrPluginContext,
) : AbstractTransformerForGenerator {

  override val keys: List<GeneratedDeclarationKey>
    get() = listOf(FirDeclarationGenerator.Key)

  override fun visitClass(declaration: IrClass) {
    if (!declaration.hasAnnotation(FqNames.EntryPoint))
      return super.visitClass(declaration)

    val properties = declaration.properties.filter { it.annotations.hasAnnotation(FqNames.Inject) }.toList()
    for (props in properties) {
      declaration.addProperty {
        name = Name.identifier(props.name.asString() + "Delegate")
        modality = Modality.FINAL
      }.apply {
        addBackingField {
          val lazyType = context.referenceClass(ClassId(FqName("kotlin"), Name.identifier("Lazy")))!!
          type = IrSimpleTypeImpl(
            classifier = lazyType,
            nullability = SimpleTypeNullability.DEFINITELY_NOT_NULL,
            arguments = listOf(
              makeTypeProjection(
                type = props.getter?.returnType!!,
                variance = Variance.INVARIANT
              )
            ),
            annotations = emptyList()
          )
        }

      }
    }
    super.visitClass(declaration)
  }
}