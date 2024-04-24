package `in`.stock.core.di.compiler.di

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import dagger.BindsInstance
import dagger.Component
import `in`.stock.core.di.compiler.ModuleProcessor
import `in`.stock.core.di.compiler.core.KspResolver

@Component(modules = [GeneratorsBinder::class])
interface CompilerComponent {

    fun injectModuleProcessor(processor: ModuleProcessor)

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance kspLogger: KSPLogger,
            @BindsInstance codeGenerator: CodeGenerator,
            @BindsInstance resolver: KspResolver
        ): CompilerComponent
    }
}