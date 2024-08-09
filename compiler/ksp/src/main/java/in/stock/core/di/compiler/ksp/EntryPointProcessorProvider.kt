package `in`.stock.core.di.compiler.ksp

import com.google.devtools.ksp.symbol.KSDeclaration
import `in`.stock.core.di.compiler.core.Generator
import `in`.stock.core.di.compiler.core.XRoundEnv

interface EntryPointProcessorProvider {
	fun create(xEnv: XRoundEnv): Generator<KSDeclaration, String>
}