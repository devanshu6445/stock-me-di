package `in`.stock.core.di.integration_tests

import `in`.stock.core.di.integration_tests.core.Function
import `in`.stock.core.di.integration_tests.core.ProjectCompiler
import `in`.stock.core.di.integration_tests.core.Target
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe

class DiModuleTest : FreeSpec({
    "Module must be object class" {
        shouldThrowExactly<Exception> {
            ProjectCompiler(
                workingDir = tempdir()
            ).source(
                fileName = "Main.kt",
                source = """
                package main
                import `in`.stock.core.di.runtime.annotations.Module
                import `in`.stock.core.di.runtime.annotations.Component

                @Module
                
                abstract class Main

            """.trimIndent()
            ).compile()
        }.message?.contains("Module must be a object class") shouldBe true
    }

    "Module installing in component" {
        ProjectCompiler()
            .source(
                fileName = "Man.kt",
                source = """
package main
import `in`.stock.core.di.runtime.annotations.Module
import `in`.stock.core.di.runtime.annotations.Component

@Module 
@InstallIn(Comp1::clas)
object Comp1Module {
    
    @Provides
    fun provideA() = A()
}

class A

@Component
abstract class Comp1 {
 abstract val a: A
}
                """.trimIndent()
            ).compile()
    }

    "Multi-gradle-module di module installation" {
        val subProject = ProjectCompiler(
            target = Target.KSP,
        )

        val mainProject = ProjectCompiler(
            target = Target.KSP,
            dependencies = listOf(subProject)
        )

        subProject.source(
            fileName = "Sub.kt",
            """
                package com.sub
                import `in`.stock.core.di.runtime.Singleton
                import `in`.stock.core.di.runtime.SingletonComponent
                import `in`.stock.core.di.runtime.annotations.InstallIn
                import `in`.stock.core.di.runtime.annotations.Module
                import me.tatarka.inject.annotations.Provides
                import `in`.stock.core.di.runtime.annotations.Component

                @InstallIn(SingletonComponent::class)
                @Singleton
                @Module
                object Subject {
                
                @Provides
                fun ab() : DepA = DepA()
                }

                class DepA

                @Component
                abstract class SubjectComponent(
                @Component val sc: SingletonComponent
                ) {
                abstract val depA: DepA
                }
            """.trimIndent()
        )

        mainProject.source(
            fileName = "Main.kt",
            """
                package com.dev
                import com.sub.SubjectComponent
                import com.sub.DepA
                import `in`.stock.core.di.runtime.annotations.Component
                import me.tatarka.inject.annotations.Inject

                @Component
                abstract class Science(
                @Component val subjectComponent1 :SubjectComponent
                ) {
                abstract val depB: DepB
                }

                @Inject
                class DepB(val depA: DepA) {
                 override fun toString(): String {return super.toString() + depA.toString()}
                }

                fun main() {
                val comp = Science::class.create() 
                println(comp.depB)
                }

            """.trimIndent()
        )

        mainProject.compile().runJvm(
            function = Function(
                className = "com.dev.MainKt",
                functionName = "main",
                args = listOf()
            )
        )
    }
})