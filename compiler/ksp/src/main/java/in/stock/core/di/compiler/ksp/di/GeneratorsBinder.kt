package `in`.stock.core.di.compiler.ksp.di

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import dagger.Binds
import dagger.Module
import `in`.stock.core.di.compiler.core.*
import `in`.stock.core.di.compiler.ksp.ModuleProcessingStepValidatorImpl
import `in`.stock.core.di.compiler.ksp.ProcessModuleImpl
import `in`.stock.core.di.compiler.ksp.data.*
import `in`.stock.core.di.compiler.ksp.generators.*

@Module
interface GeneratorsBinder {

  @Binds
  fun bindModuleGenerator(moduleGenerator: ModuleGenerator): Generator<ModuleInfo, Unit>

  @Binds
  fun providerGenerator(providerGenerator: ProviderGenerator): Generator<ProvidesInfo, Unit>

  @Binds
  fun bindComponentGenerator(componentGenerator: ComponentGenerator): Generator<ComponentInfo, ComponentGeneratorResult>

  @Binds
  fun bindModuleProviderGenerator(
    moduleProviderGenerator: ModuleProviderGenerator
  ): Generator<ModuleInfo, ModuleProviderResult>

  @Binds
  fun bindModuleProviderRegistryGenerator(
    moduleProviderRegistryGenerator: ModuleProviderRegistryGenerator
  ): Generator<List<ModuleProviderResult>, Unit>

  @Binds
  fun bindCodeGenerator(flexibleCodeGeneratorImpl: FlexibleCodeGeneratorImpl): FlexibleCodeGenerator

  @Binds
  fun bindEntryPointGenerator(entryPointGenerator: EntryPointGenerator): Generator<KSDeclaration, Unit>

  @Binds
  fun bindProcessModule(processModuleImpl: ProcessModuleImpl): ProcessingStep<KSClassDeclaration, Pair<ModuleInfo, ModuleProviderResult>>

  @Binds fun bindModuleValidator(moduleValidatorImpl: ModuleProcessingStepValidatorImpl): ProcessingStepValidator<KSClassDeclaration>
}