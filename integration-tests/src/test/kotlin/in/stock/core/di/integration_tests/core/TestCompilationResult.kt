package `in`.stock.core.di.integration_tests.core

import com.tschuchort.compiletesting.KotlinCompilation
import java.io.File
import java.net.URLClassLoader
import javax.tools.Diagnostic

class TestCompilationResult(
  private val result: KotlinCompilation.Result,
  private val compiledSource: List<File>,
) {
  val success: Boolean
    get() = result.exitCode == KotlinCompilation.ExitCode.OK

  fun output(kind: Diagnostic.Kind): String = result.messages.filterByKind(kind)

  fun runJvm(function: Function) {
    val classLoader = URLClassLoader(
      compiledSource.map { it.toURI().toURL() }.toTypedArray(),
      this.javaClass.classLoader
    )

    val entryClass = classLoader.loadClass(function.className)

    val entryFunction = entryClass.declaredMethods.singleOrNull {
      it.name == function.functionName && it.parameterCount == function.args.size
    }

    check(entryFunction != null) {
      "Cannot find method '${function.functionName}' in '${function.className}' with ${function.args.size} parameters."
    }

    val args = entryFunction.parameterTypes.zip(function.args) { type, arg ->
      type.valueOf(arg)
    }.toTypedArray()

    try {
      entryFunction.invoke(null, *args)
    } catch (t: Throwable) {
      t.printStackTrace()
    }
  }
}

data class Function(
  val className: String,
  val functionName: String,
  val args: List<String>
)

private fun <T> Class<T>.valueOf(value: String): T? {
  if (value == "null") return null
  return when (this) {
    Boolean::class.java -> value.toBoolean()
    Char::class.java -> value[0]
    Short::class.java -> value.toShort()
    Int::class.java -> value.toInt()
    Long::class.java -> value.toLong()
    Float::class.java -> value.toFloat()
    Double::class.java -> value.toDouble()
    String::class.java -> value
    else -> throw IllegalArgumentException("Parameter type $this is not supported.")
  } as T
}