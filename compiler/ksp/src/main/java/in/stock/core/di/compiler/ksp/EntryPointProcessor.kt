package `in`.stock.core.di.compiler.ksp

import com.google.devtools.ksp.symbol.KSDeclaration
import `in`.stock.core.di.compiler.core.XRoundEnv

interface EntryPointProcessor {
	fun isApplicable(xRoundEnv: XRoundEnv, node: KSDeclaration): Boolean
	fun process(xRoundEnv: XRoundEnv, node: KSDeclaration)
}