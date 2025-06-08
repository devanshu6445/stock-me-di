package `in`.kdi.compiler.ksp.di

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import dagger.Binds
import dagger.Module
import `in`.kdi.compiler.core.Generator
import `in`.kdi.compiler.core.ProcessingStepValidator
import `in`.kdi.compiler.core.XProcessingStepVoid
import `in`.kdi.compiler.ksp.data.*
import `in`.kdi.compiler.ksp.generators.ComponentGenerator
import `in`.kdi.compiler.ksp.generators.ModuleProviderGenerator
import `in`.kdi.compiler.ksp.generators.ProviderGenerator
import `in`.kdi.compiler.ksp.steps.EntryPointProcessingStep
import `in`.kdi.compiler.ksp.steps.ModuleProcessingStep
import `in`.kdi.compiler.ksp.validators.ModuleValidator

@Module
interface GeneratorsBinder {

	@Binds
	fun providerGenerator(
		providerGenerator: ProviderGenerator
	): Generator<ProvidesInfo, Unit>

	@Binds
	fun bindComponentGenerator(
		componentGenerator: ComponentGenerator
	): Generator<ComponentInfo, ComponentGeneratorResult>

	@Binds
	fun bindModuleProviderGenerator(
		moduleProviderGenerator: ModuleProviderGenerator
	): Generator<ModuleInfo, ModuleProviderResult>

	@Binds
	fun bindEntryPointGenerator(
		entryPointProcessingStep: EntryPointProcessingStep
	): XProcessingStepVoid<KSDeclaration, Unit>

	@Binds
	fun bindProcessModule(
		moduleProcessingStep: ModuleProcessingStep
	): XProcessingStepVoid<KSClassDeclaration, Pair<ModuleInfo, ModuleProviderResult>>

	@Binds
	fun bindModuleValidator(
		moduleValidatorImpl: ModuleValidator
	): ProcessingStepValidator<KSClassDeclaration>
}