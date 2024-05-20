package `in`.stock.core.di.kotlin_di_compiler.backend

import `in`.stock.core.di.kotlin_di_compiler.backend.core.AbstractTransformerForGenerator
import `in`.stock.core.di.kotlin_di_compiler.k2.FirDeclarationGenerator
import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrClass

class InjectIrPropertyGenerator(
  override val context: IrPluginContext,
) : AbstractTransformerForGenerator {

  override val keys: List<GeneratedDeclarationKey>
    get() = listOf(FirDeclarationGenerator.Key)

  override fun visitClass(declaration: IrClass) {


    super.visitClass(declaration)
  }
}