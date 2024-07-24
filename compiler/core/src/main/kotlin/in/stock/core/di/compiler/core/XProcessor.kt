package `in`.stock.core.di.compiler.core

import com.google.devtools.ksp.symbol.KSAnnotated

interface XProcessor {
	val xEnv: XEnv

	fun preRound(xRoundEnv: XRoundEnv)
	fun processSymbol(xRoundEnv: XRoundEnv, symbol: KSAnnotated, annotationFullName: String): Boolean
	fun postRound(xRoundEnv: XRoundEnv)
}