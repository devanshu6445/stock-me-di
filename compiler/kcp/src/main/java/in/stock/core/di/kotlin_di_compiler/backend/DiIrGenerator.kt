package `in`.stock.core.di.kotlin_di_compiler.backend

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid

// todo add field injection support and no arg constructor for non primary constructor classes

open class DiIrGenerator : IrGenerationExtension {
  override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
    moduleFragment.acceptChildrenVoid(
      EntryPointIrGenerator(
        context = pluginContext,
        entryPointConstructorIrTransformer = EntryPointConstructorIrTransformer(context = pluginContext),
        injectPropertyIrTransformer = InjectPropertyIrTransformer(context = pluginContext),
        injectPropertyGetterSetterIrTransformer = InjectPropertyGetterSetterIrTransformer(context = pluginContext),
        injectIrPropertyGenerator = InjectIrPropertyGenerator(context = pluginContext)
      )
    )
  }
}