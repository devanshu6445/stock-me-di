package `in`.stock.core.di.compiler.ksp.di

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import dagger.Binds
import dagger.Module
import `in`.stock.core.di.compiler.core.Generator
import `in`.stock.core.di.compiler.core.ProcessingStepValidator
import `in`.stock.core.di.compiler.core.XProcessingStepVoid
import `in`.stock.core.di.compiler.ksp.data.*
import `in`.stock.core.di.compiler.ksp.generators.*
import `in`.stock.core.di.compiler.ksp.steps.ModuleProcessingStep
import `in`.stock.core.di.compiler.ksp.validators.ModuleValidator

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
	fun bindEntryPointGenerator(entryPointGenerator: EntryPointGenerator): Generator<KSDeclaration, Unit>

	@Binds
	fun bindProcessModule(
		moduleProcessingStep: ModuleProcessingStep
	): XProcessingStepVoid<KSClassDeclaration, Pair<ModuleInfo, ModuleProviderResult>>

	@Binds
	fun bindModuleValidator(
		moduleValidatorImpl: ModuleValidator
	): ProcessingStepValidator<KSClassDeclaration>
}