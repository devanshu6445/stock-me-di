package `in`.stock.core.di.kotlin_di_compiler.backend

import `in`.stock.core.di.kotlin_di_compiler.backend.core.AbstractTransformerForGenerator
import `in`.stock.core.di.kotlin_di_compiler.builders.declarations.addBackingField
import `in`.stock.core.di.kotlin_di_compiler.builders.declarations.addProperty
import `in`.stock.core.di.kotlin_di_compiler.utils.FqNames
import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.types.starProjectedType
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.properties
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

class InjectIrPropertyGenerator(
  override val context: IrPluginContext,
  override val keys: List<GeneratedDeclarationKey>
) : AbstractTransformerForGenerator {

  val a by lazy { }

  override fun visitClass(declaration: IrClass) {
    super.visitClass(declaration)

    for (props in declaration.properties.filter { it.annotations.hasAnnotation(FqNames.Inject) }) {
      declaration.addProperty {
        modality = Modality.FINAL
      }.apply {
        addBackingField {
          val lazyType = context.referenceClass(ClassId(FqName("kotlin"), Name.identifier("Lazy")))?.starProjectedType
          type = lazyType!!
        }
      }
    }
  }
}