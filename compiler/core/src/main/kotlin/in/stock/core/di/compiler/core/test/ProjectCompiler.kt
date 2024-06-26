@file:OptIn(ExperimentalCompilerApi::class)

package `in`.stock.core.di.compiler.core.test

import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.symbolProcessorProviders
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import java.io.File
import javax.tools.Diagnostic

class ProjectCompiler(
	private val workingDir: File? = null,
	private val dependencies: List<ProjectCompiler> = listOf()
) {

	private val sourceFiles = mutableListOf<SourceFile>()

	private val classpath: MutableList<File> = mutableListOf()

	private val compilation = newCompilation()

	// Created a separate ksp compilation to generate the code first because the generate code
	// was not being resolved in the same compilation round
	// todo: Try to convert it into single compilation round
	private val kspCompilation = newCompilation {
		if (this@ProjectCompiler.workingDir != null) {
			workingDir = this@ProjectCompiler.workingDir
		}

		messageOutputStream = System.out
	}

	fun source(fileName: String, @Language("kotlin") source: String): ProjectCompiler {
		sourceFiles.add(SourceFile.kotlin(fileName, source))
		return this
	}

	fun source(fileName: String): ProjectCompiler {
		sourceFiles.add(
			SourceFile.fromPath(
				path = File(fileName)
			)
		)
		return this
	}

	fun symbolProcessor(processor: SymbolProcessorProvider): ProjectCompiler {
		kspCompilation.symbolProcessorProviders += processor
		return this
	}

	@OptIn(ExperimentalCompilerApi::class)
	fun compilerPlugin(registrar: CompilerPluginRegistrar): ProjectCompiler {
		compilation.compilerPluginRegistrars += registrar
		return this
	}

	fun commandLineProcessor(commandLineProcessor: CommandLineProcessor): ProjectCompiler {
		compilation.commandLineProcessors += commandLineProcessor
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

		// First generate code by kspCompilation
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