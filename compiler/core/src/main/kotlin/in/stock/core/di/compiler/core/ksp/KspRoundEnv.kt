package `in`.stock.core.di.compiler.core.ksp

import `in`.stock.core.di.compiler.core.XEnv
import `in`.stock.core.di.compiler.core.XRoundEnv

class KspRoundEnv(override val xEnv: XEnv, override val isLastRound: Boolean) : XRoundEnv
