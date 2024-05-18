package `in`.stock.core.di.integration_tests.core

import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.symbolProcessorProviders
import `in`.stock.core.di.compiler.ModuleProcessor
import me.tatarka.inject.compiler.ksp.InjectProcessorProvider
import org.intellij.lang.annotations.Language
import java.io.File
import javax.tools.Diagnostic

class ProjectCompiler(
  private val target: Target = Target.KSP,
  private val workingDir: File? = null,
  private val dependencies: List<ProjectCompiler> = listOf()
) {

  private val sourceFiles = mutableListOf<SourceFile>()
  private val symbolProcessors = mutableListOf<SymbolProcessorProvider>()

  private val classpath: MutableList<File> = mutableListOf()

  private val compilation = newCompilation()

  // Created a separate ksp compilation to generate the code first because the generate code
  // was not being resolved in the same compilation round
  // todo: Try to convert it into single compilation round
  private val kspCompilation = newCompilation {
    if (this@ProjectCompiler.workingDir != null) {
      workingDir = this@ProjectCompiler.workingDir
    }

    when (target) {
      Target.KSP -> {
        symbolProcessorProviders = mutableListOf<SymbolProcessorProvider>().apply {
          add(ModuleProcessor.Provider())
          add(InjectProcessorProvider())
          addAll(symbolProcessors)
        }
      }
    }

    messageOutputStream = System.out
  }

  fun source(fileName: String, @Language("kotlin") source: String): ProjectCompiler {
    sourceFiles.add(SourceFile.kotlin(fileName, source))
    return this
  }

  fun symbolProcessor(processor: SymbolProcessorProvider): ProjectCompiler {
    symbolProcessors.add(processor)
    return this
  }

  private fun compileDependencies() {
    dependencies.forEach {
      it.compile()
      classpath += it.compilation.classesDir // todo check if correct classesDir is being used
      classpath += it.compilation.classpaths
    }
  }

  fun compile(): TestCompilationResult {
    compileDependencies()

    // First generate generate code by kspCompilation
    val kspCompilationResult = kspCompilation.compile()

    if (kspCompilationResult.exitCode != KotlinCompilation.ExitCode.OK) {
      throw ProjectCompilationException(
        diagnosticInfo = kspCompilationResult.toTestCompilationResult().output(Diagnostic.Kind.ERROR)
      )
    }

    // Add the generated code sources from kspCompilation to final round of compilation
    compilation.sources += kspCompilation.kspSourcesDir.walkTopDown()
      .filter { !it.isDirectory }
      .map {
        SourceFile.new(it.name, it.readTextAndUnify())
      }

    val result = compilation.compile().toTestCompilationResult()

    if (!result.success) {
      throw ProjectCompilationException(result.output(Diagnostic.Kind.ERROR))
    }
    return result
  }

  private fun newCompilation(block: KotlinCompilation.() -> Unit = {}) =
    KotlinCompilation().also { compilation ->
      compilation.verbose = false
      compilation.inheritClassPath = true
      compilation.classpaths = classpath
      compilation.sources = sourceFiles

      compilation.block()
    }

  private fun KotlinCompilation.Result.toTestCompilationResult() =
    TestCompilationResult(result = this, compiledSource = classpath + compilation.classesDir)
}