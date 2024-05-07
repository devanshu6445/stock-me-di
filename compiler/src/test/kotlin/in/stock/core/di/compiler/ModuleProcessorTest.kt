package `in`.stock.core.di.compiler

import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.tschuchort.compiletesting.*
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldNotBe
import java.io.File

class ModuleProcessorTest : FreeSpec({
  "test-1" {
    val compilation = prepareCompilation(
      SourceFile.kotlin(
        "Test.kt", """
            package main

            import `in`.stock.core.di.runtime.Module

            @Module            
            class HttpClient 
        """.trimIndent()
      ),
      providers = listOf(ModuleProcessor.Provider())
    ).apply {
      compile()
    }

    val files = compilation.findGeneratedFiles()

    println(files)

    files.size shouldNotBe 0
  }
})

fun prepareCompilation(
  vararg sourceFiles: SourceFile,
  providers: List<SymbolProcessorProvider>
): KotlinCompilation =
  KotlinCompilation()
    .apply {
      inheritClassPath = true
      symbolProcessorProviders = providers
      sources = sourceFiles.asList()
      verbose = true
      kspIncremental = true
    }

fun KotlinCompilation.findGeneratedFiles(): List<File> {
  return kspSourcesDir
    .walkTopDown()
    .filter { it.isFile }
    .toList()
}