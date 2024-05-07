package `in`.stock.core.di.compiler.core.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSVisitor
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.ClassName
import `in`.stock.core.di.compiler.core.*
import kotlin.reflect.KClass

abstract class KspBaseProcessor(
  private val environment: SymbolProcessorEnvironment,
) : SymbolProcessor, KspLifecycle {

  private var internalState: KspLifecycle.State = KspLifecycle.State.None

  override val state: KspLifecycle.State
    get() = internalState

  protected val logger: KSPLogger
    get() = environment.logger

  protected val messenger: Messenger by lazy {
    MessengerImpl(logger)
  }

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

    val deferredSymbols = onProcessSymbols(kspResolver)

    internalState = KspLifecycle.State.Processed

    return deferredSymbols
  }

  protected inline fun <reified T : KSNode> Resolver.getSymbols(cls: KClass<*>) =
    this.getSymbolsWithAnnotation(cls.qualifiedName.orEmpty())
      .filterIsInstance<T>()
      .filter(KSNode::validate)

  protected inline fun <reified T : KSNode> Resolver.getSymbols(className: ClassName) =
    this.getSymbolsWithAnnotation(className.canonicalName)
      .filterIsInstance<T>()
      .filter(KSNode::validate)

  protected inline fun <reified T : KSNode> Resolver.getSymbols(
    cls: KClass<*>,
    validator: KSVisitor<Unit, Unit>,
  ) =
    this.getSymbolsWithAnnotation(cls.qualifiedName.orEmpty())
      .filterIsInstance<T>()
      .filter(KSNode::validate)
      .apply {
        forEach {
          it.accept(validator, Unit)
        }
      }

  final override fun finish() {
    super.finish()
    onFinished()
    internalState = KspLifecycle.State.Finished
  }

  override fun onFinished() {}
}