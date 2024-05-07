package `in`.stock.core.di.compiler.core.exceptions

/**
 * Exception for when the class is not constructable via the Compiler
 */
class ClassConstructException(override val message: String?) : Exception()