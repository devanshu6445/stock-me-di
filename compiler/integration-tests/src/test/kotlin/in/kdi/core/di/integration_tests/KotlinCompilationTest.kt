package `in`.kdi.core.di.integration_tests

import `in`.kdi.core.di.compiler.core.test.Function
import `in`.stock.core.di.compiler.core.test.ProjectCompiler
import io.kotest.core.spec.style.FreeSpec

class KotlinCompilationTest : FreeSpec({

  "Multi-module compilation test" {
    val subProject = ProjectCompiler()

    val mainProject = ProjectCompiler(
      dependencies = listOf(subProject)
    )

    subProject.source(
      fileName = "Sub.kt",
      """
                package com.sub
                
                fun function1() {
                    println("function 1")
                }
                
            """.trimIndent()
    )

    mainProject.source(
      fileName = "Main.kt",
      """
                package com.dev
                import com.sub.function1

                fun main() {
                println("Main function")
                function1()
                }

            """.trimIndent()
    )

    mainProject.compile().runStaticFunction(
      function = `in`.kdi.core.di.compiler.core.test.Function(
          className = "com.dev.MainKt",
          functionName = "main",
          args = listOf()
      )
    )
  }
})