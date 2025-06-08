package `in`.kdi.core.di.compiler.ksp

import `in`.stock.core.di.compiler.core.XRoundEnv

interface EntryPointProcessorProvider {
	fun create(xEnv: XRoundEnv): EntryPointProcessor
}