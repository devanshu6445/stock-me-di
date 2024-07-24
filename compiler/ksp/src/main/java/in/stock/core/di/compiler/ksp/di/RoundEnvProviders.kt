package `in`.stock.core.di.compiler.ksp.di

import dagger.Module
import dagger.Provides
import `in`.stock.core.di.compiler.core.*

@Module
class RoundEnvProviders {

	@Provides
	fun bindCodeGenerator(xRoundEnv: XRoundEnv): XCodeGenerator {
		return xRoundEnv.xEnv.codeGenerator
	}

	@Provides
	fun bindXResolver(xRoundEnv: XRoundEnv): XResolver = xRoundEnv.xEnv.resolver

	@Provides
	fun bindMessenger(xRoundEnv: XRoundEnv): Messenger = xRoundEnv.xEnv.messenger

	@Provides
	fun bindXEnv(xRoundEnv: XRoundEnv): XEnv = xRoundEnv.xEnv
}