package `in`.stock.core.di.compiler.di

import com.google.devtools.ksp.symbol.KSDeclaration
import dagger.Binds
import dagger.Module
import `in`.stock.core.di.compiler.core.FlexibleCodeGenerator
import `in`.stock.core.di.compiler.core.FlexibleCodeGeneratorImpl
import `in`.stock.core.di.compiler.core.Generator
import `in`.stock.core.di.compiler.data.*
import `in`.stock.core.di.compiler.generators.*

@Module
interface GeneratorsBinder {

  @Binds
  fun bindModuleGenerator(moduleGenerator: ModuleGenerator): Generator<ModuleInfo, Unit>

  @Binds
  fun providerGenerator(providerGenerator: ProviderGenerator): Generator<ProvidesInfo, Unit>

  @Binds
  fun bindComponentGenerator(componentGenerator: ComponentGenerator): Generator<ComponentInfo, ComponentGeneratorResult>

  @Binds
  fun bindModuleProviderGenerator(moduleProviderGenerator: ModuleProviderGenerator): Generator<ModuleInfo, ModuleProviderResult>

  @Binds
  fun bindModuleProviderRegistryGenerator(moduleProviderRegistryGenerator: ModuleProviderRegistryGenerator): Generator<List<ModuleProviderResult>, Unit>

  @Binds
  fun bindCodeGenerator(flexibleCodeGeneratorImpl: FlexibleCodeGeneratorImpl): FlexibleCodeGenerator

  @Binds
  fun bindEntryPointGenerator(entryPointGenerator: EntryPointGenerator): Generator<Sequence<KSDeclaration>, Unit>
}