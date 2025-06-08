package `in`.kdi.compiler.ksp.steps

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import `in`.kdi.compiler.core.XProcessingStepVoid
import `in`.kdi.compiler.core.XRoundEnv
import `in`.kdi.compiler.ksp.EntryPointProcessorProvider
import `in`.kdi.compiler.ksp.android.ViewModelFactoryGenerator
import `in`.kdi.compiler.ksp.generators.EntryPointComponentGenerator
import `in`.kdi.compiler.ksp.generators.EntryPointInjectorGenerator
import `in`.kdi.compiler.ksp.validators.EntryPointValidator
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