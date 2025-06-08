package `in`.kdi.compiler.ksp

import `in`.kdi.compiler.core.XRoundEnv

interface EntryPointProcessorProvider {
	fun create(xEnv: XRoundEnv): EntryPointProcessor
}