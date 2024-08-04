package `in`.stock.core.di.compiler.core.ext

import com.google.devtools.ksp.processing.Dependencies
import com.squareup.kotlinpoet.FileSpec
import `in`.stock.core.di.compiler.core.XCodeGenerator
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

fun FileSpec.writeTo(
	codeGenerator: XCodeGenerator,
	dependencies: Dependencies = Dependencies(true),
) {
  writeTo(
    codeGenerator = codeGenerator,
    packageName = packageName,
    fileName = name,
    dependencies = dependencies
  )
}

fun FileSpec.writeTo(
	codeGenerator: XCodeGenerator,
	packageName: String,
	fileName: String,
	extension: String = "kt",
	dependencies: Dependencies = Dependencies(true),
) {
  val file = codeGenerator.createNewFile(
    dependencies = dependencies,
    packageName = packageName,
    fileName = fileName,
    extensionName = extension
  )
  OutputStreamWriter(file, StandardCharsets.UTF_8)
    .use(::writeTo)
}
