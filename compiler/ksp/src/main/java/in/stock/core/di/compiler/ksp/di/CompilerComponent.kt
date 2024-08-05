package `in`.stock.core.di.compiler.ksp.di

import dagger.BindsInstance
import dagger.Component
import `in`.stock.core.di.compiler.core.XRoundEnv
import `in`.stock.core.di.compiler.ksp.DIProcessor
import javax.inject.Singleton

@Singleton
@Component(modules = [GeneratorsBinder::class, RoundEnvProviders::class])
interface CompilerComponent {

	fun injectModuleProcessor(processor: DIProcessor)

  @Component.Factory
  interface Factory {
    fun create(
			@BindsInstance xRoundEnv: XRoundEnv
    ): CompilerComponent
  }
}