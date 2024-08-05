package `in`.stock.core.di.compiler.core.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import `in`.stock.core.di.compiler.core.XCodeGenerator
import java.io.File
import java.io.OutputStream
import javax.inject.Inject

class KSPCodeGenerator @Inject constructor(private val delegate: CodeGenerator) :
	XCodeGenerator {
  override val generatedFile: Collection<File>
    get() = delegate.generatedFile

  override fun associate(
    sources: List<KSFile>,
    packageName: String,
    fileName: String,
    extensionName: String,
  ) {
    delegate.associate(sources, packageName, fileName, extensionName)
  }

  override fun associateByPath(sources: List<KSFile>, path: String, extensionName: String) {
    delegate.associateByPath(sources, path, extensionName)
  }

  override fun associateWithClasses(
    classes: List<KSClassDeclaration>,
    packageName: String,
    fileName: String,
    extensionName: String,
  ) {
    delegate.associateWithClasses(classes, packageName, fileName, extensionName)
  }

  override fun createNewFile(
    dependencies: Dependencies,
    packageName: String,
    fileName: String,
    extensionName: String,
  ): OutputStream {
    return delegate.createNewFile(dependencies, packageName, fileName, extensionName)
  }

  override fun createNewFileByPath(
    dependencies: Dependencies,
    path: String,
    extensionName: String,
  ): OutputStream {
    return delegate.createNewFileByPath(dependencies, path, extensionName)
  }
}