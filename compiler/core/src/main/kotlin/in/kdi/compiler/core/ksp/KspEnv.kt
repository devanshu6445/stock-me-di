package `in`.kdi.compiler.core.ksp

import com.google.devtools.ksp.processing.JvmPlatformInfo
import com.google.devtools.ksp.processing.PlatformInfo
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import `in`.kdi.compiler.core.KspResolver
import `in`.kdi.compiler.core.XCodeGenerator
import `in`.kdi.compiler.core.XEnv
import `in`.kdi.compiler.core.XResolver

class KspEnv(
	environment: SymbolProcessorEnvironment
) : XEnv {
	override val messenger = MessengerImpl(environment.logger)

	override val platforms: List<PlatformInfo> by lazy {
		environment.platforms
	}

	override val jvmPlatformInfo: JvmPlatformInfo? by lazy {
		platforms.filterIsInstance<JvmPlatformInfo>()
			.firstOrNull()
	}

	internal var _resolver: KspResolver? = null

	override val resolver: XResolver
		get() = _resolver!!

	override val codeGenerator: XCodeGenerator = KSPCodeGenerator(
		environment.codeGenerator
	)

	override val jvmVersion: Int by lazy {
		when (jvmPlatformInfo?.jvmTarget) {
			"1.8", null -> 8
			else -> jvmPlatformInfo!!.jvmTarget.toInt()
		}
	}
}