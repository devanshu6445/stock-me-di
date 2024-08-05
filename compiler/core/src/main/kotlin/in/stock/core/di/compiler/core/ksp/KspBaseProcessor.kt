package `in`.stock.core.di.compiler.core.ksp

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import `in`.stock.core.di.compiler.core.KspResolver
import `in`.stock.core.di.compiler.core.XProcessor

abstract class KspBaseProcessor(
	private val environment: SymbolProcessorEnvironment,
) : SymbolProcessor, XProcessor {

	override val xEnv: KspEnv = KspEnv(
		environment = environment
	)

	// todo can add a check to process all the annotations parallel
	protected abstract val annotations: List<String>

	final override fun process(resolver: Resolver): List<KSAnnotated> {
		xEnv._resolver = KspResolver(resolver)

		val deferredSymbols = mutableListOf<KSAnnotated>()

		val kspRoundEnv = KspRoundEnv(
			isLastRound = false,
			xEnv = xEnv
		)

		preRound(kspRoundEnv)

		annotations.forEach { qualifiedName ->
			for (element in kspRoundEnv.xEnv.resolver.getSymbolsWithAnnotation(qualifiedName)) {
				if (!processSymbol(kspRoundEnv, symbol = element, annotationFullName = qualifiedName)) {
					deferredSymbols += element
				}
			}
		}

		postRound(kspRoundEnv)
		return deferredSymbols
	}
}