package `in`.stock.core.di.compiler.ksp.di

import `in`.stock.core.di.compiler.core.XProcessor
import `in`.stock.core.di.compiler.ksp.DIProcessor

class ProcessorMapper(
	private val daggerCompilerComponent: CompilerComponent,
	private val processor: XProcessor,
) {

  fun injectProcessors() {
    when (processor) {
			is DIProcessor -> {
        daggerCompilerComponent.injectModuleProcessor(
          processor = processor,
        )
      }
    }
  }
}