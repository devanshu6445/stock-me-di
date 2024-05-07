package `in`.stock.core.di.compiler.core

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSVisitor
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.ClassName
import `in`.stock.core.di.compiler.di.DaggerCompilerComponent
import `in`.stock.core.di.compiler.di.ProcessorMapper
import kotlin.reflect.KClass

abstract class KspBaseProcessor(
  private val environment: SymbolProcessorEnvironment,
) : SymbolProcessor {

  protected val logger: KSPLogger
    get() = environment.logger

  final override fun process(resolver: Resolver): List<KSAnnotated> {
    //Injecting the child classes pf KspBaseProcessor by using a ProcessorMapper
    ProcessorMapper(
      DaggerCompilerComponent.factory().create(
        kspLogger = logger,
        codeGenerator = FlexibleCodeGeneratorImpl(delegate = environment.codeGenerator),
        resolver = KspResolver(delegate = resolver)
      ), this
    ).injectProcessors()
    //Do the actual processing
    return processSymbols(resolver)
  }

  abstract fun processSymbols(resolver: Resolver): List<KSAnnotated>

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
}