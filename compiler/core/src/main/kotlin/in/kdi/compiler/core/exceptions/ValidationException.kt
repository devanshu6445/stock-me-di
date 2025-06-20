package `in`.kdi.compiler.core.exceptions

import com.google.devtools.ksp.symbol.KSNode

class ValidationException(override val message: String?, val node: KSNode) : IllegalStateException()