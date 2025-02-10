package `in`.stock.core.di.compiler.core

import com.google.devtools.ksp.processing.JvmPlatformInfo
import com.google.devtools.ksp.processing.PlatformInfo

interface XEnv {
	val messenger: Messenger
	val resolver: XResolver
	val codeGenerator: XCodeGenerator

	val jvmVersion: Int
	val jvmPlatformInfo: JvmPlatformInfo?
	val platforms: List<PlatformInfo>
}