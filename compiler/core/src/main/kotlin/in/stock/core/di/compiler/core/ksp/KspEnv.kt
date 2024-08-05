package `in`.stock.core.di.compiler.core.ksp

import com.google.devtools.ksp.processing.JvmPlatformInfo
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import `in`.stock.core.di.compiler.core.KspResolver
import `in`.stock.core.di.compiler.core.XCodeGenerator
import `in`.stock.core.di.compiler.core.XEnv
import `in`.stock.core.di.compiler.core.XResolver

class KspEnv(
	environment: SymbolProcessorEnvironment
) : XEnv {
	override val messenger = MessengerImpl(environment.logger)

	override val jvmPlatformInfo: JvmPlatformInfo? = environment.platforms.filterIsInstance<JvmPlatformInfo>()
		.firstOrNull()

	internal var _resolver: KspResolver? = null

	override val resolver: XResolver
		get() = _resolver!!

	override val codeGenerator: XCodeGenerator = KSPCodeGenerator(
		environment.codeGenerator
	)

	override val jvmVersion: Int by lazy {
		when (jvmPlatformInfo?.jvmTarget) {
			"1.8", null -> 8
			else -> jvmPlatformInfo.jvmTarget.toInt()
		}
	}
}