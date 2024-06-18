package `in`.stock.core.di.compiler.ksp.di

import `in`.stock.core.di.compiler.ksp.ModuleProcessor
import `in`.stock.core.di.compiler.core.processor.KspBaseProcessor

class ProcessorMapper(
  private val daggerCompilerComponent: CompilerComponent,
  private val processor: KspBaseProcessor,
) {

  fun injectProcessors() {
    when (processor) {
      is ModuleProcessor -> {
        daggerCompilerComponent.injectModuleProcessor(
          processor = processor,
        )
      }
    }
  }
}