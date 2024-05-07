package `in`.stock.core.di.compiler.ksp.di

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import dagger.BindsInstance
import dagger.Component
import `in`.stock.core.di.compiler.ksp.ModuleProcessor
import `in`.stock.core.di.compiler.core.KspResolver
import `in`.stock.core.di.compiler.core.Messenger
import javax.inject.Singleton

@Singleton
@Component(modules = [GeneratorsBinder::class])
interface CompilerComponent {

  fun injectModuleProcessor(processor: ModuleProcessor)

  @Component.Factory
  interface Factory {
    fun create(
      @BindsInstance kspLogger: KSPLogger,
      @BindsInstance codeGenerator: CodeGenerator,
      @BindsInstance resolver: KspResolver,
      @BindsInstance messenger: Messenger
    ): CompilerComponent
  }
}