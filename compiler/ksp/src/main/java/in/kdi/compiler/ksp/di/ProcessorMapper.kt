package `in`.kdi.compiler.ksp.di

import `in`.kdi.compiler.core.XProcessor
import `in`.kdi.compiler.ksp.DIProcessor

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