package `in`.kdi.compiler.core.ksp

import `in`.kdi.compiler.core.XEnv
import `in`.kdi.compiler.core.XRoundEnv

class KspRoundEnv(override val xEnv: XEnv, override val isLastRound: Boolean) : XRoundEnv