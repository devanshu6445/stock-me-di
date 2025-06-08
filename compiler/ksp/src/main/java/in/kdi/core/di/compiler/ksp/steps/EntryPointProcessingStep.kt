package `in`.kdi.core.di.compiler.ksp.steps

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import `in`.stock.core.di.compiler.core.XProcessingStepVoid
import `in`.stock.core.di.compiler.core.XRoundEnv
import `in`.stock.core.di.compiler.ksp.EntryPointProcessorProvider
import `in`.stock.core.di.compiler.ksp.android.ViewModelFactoryGenerator
import `in`.stock.core.di.compiler.ksp.generators.EntryPointComponentGenerator
import `in`.stock.core.di.compiler.ksp.generators.EntryPointInjectorGenerator
import `in`.stock.core.di.compiler.ksp.validators.EntryPointValidator
import java.util.*
import javax.inject.Inject

class EntryPointProcessingStep @Inject constructor(
	private val entryPointComponentGenerator: EntryPointComponentGenerator,
	private val entryPointInjectorGenerator: EntryPointInjectorGenerator,
	private val viewModelFactoryGenerator: ViewModelFactoryGenerator,
	private val xRoundEnv: XRoundEnv,
	entryPointValidator: EntryPointValidator
) : XProcessingStepVoid<KSDeclaration, Unit>(entryPointValidator) {
	override fun step(node: KSDeclaration) {
		entryPointComponentGenerator.generate(node)

		// Generate the injector class for the entry point
		when (node) {
			is KSClassDeclaration -> {
				val entryPointProcessors = ServiceLoader.load(EntryPointProcessorProvider::class.java)
					.map { it.create(xRoundEnv) } + entryPointInjectorGenerator + viewModelFactoryGenerator
				// load the different providers from the consuming library
				for (entryPointProcessor in entryPointProcessors) {
					// Let all the applicable processor process the node
					if (entryPointProcessor.isApplicable(xRoundEnv, node)) {
						entryPointProcessor.process(xRoundEnv, node)
						// break
					}
				}
			}

			else -> {
				Unit
			}
		}
	}
}