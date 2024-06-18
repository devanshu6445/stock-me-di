package `in`.stock.core.di.compiler.core.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.squareup.kotlinpoet.ClassName
import `in`.stock.core.di.compiler.core.*

abstract class KspBaseProcessor(
  private val environment: SymbolProcessorEnvironment,
) : SymbolProcessor, KspLifecycle {

  private var internalState: KspLifecycle.State = KspLifecycle.State.None

  // todo make this state observable
  override val state: KspLifecycle.State
    get() = internalState

  protected val logger: KSPLogger
    get() = environment.logger

  protected val messenger: Messenger by lazy {
    MessengerImpl(logger)
  }

  // todo can add a check to process all the annotations parallel
  protected abstract val annotations: List<ClassName>

  protected val codeGenerator: FlexibleCodeGenerator by lazy {
    FlexibleCodeGeneratorImpl(environment.codeGenerator)
  }

  override fun onCreate(resolver: KspResolver) {}

  final override fun process(resolver: Resolver): List<KSAnnotated> {
    val kspResolver = KspResolver(resolver)
    if (internalState < KspLifecycle.State.Created) {
      onCreate(kspResolver)
      internalState = KspLifecycle.State.Created
    }
    internalState = KspLifecycle.State.Processing

    val deferredSymbols = mutableListOf<KSAnnotated>()

    annotations.forEach {
      for (element in kspResolver.getSymbolsWithAnnotation(it.canonicalName)) {
        if (!processSymbol(kspResolver, symbol = element, annotation = it)) {
          deferredSymbols += element
        }
      }
    }
    internalState = KspLifecycle.State.Processed
    roundFinished()

    return deferredSymbols
  }

  final override fun finish() {
    super.finish()
    onFinished()
    internalState = KspLifecycle.State.Finished
  }

  override fun onFinished() {}

  override fun roundFinished() {}
}