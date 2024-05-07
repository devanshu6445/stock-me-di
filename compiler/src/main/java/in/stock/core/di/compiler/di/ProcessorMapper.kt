package `in`.stock.core.di.compiler.di

import `in`.stock.core.di.compiler.ModuleProcessor
import `in`.stock.core.di.compiler.core.KspBaseProcessor

class ProcessorMapper(
  private val daggerCompilerComponent: CompilerComponent,
  private val processor: KspBaseProcessor,
) {

  fun injectProcessors() {
    when (processor) {
      is ModuleProcessor -> daggerCompilerComponent.injectModuleProcessor(
        processor = processor,
      )
    }
  }
}