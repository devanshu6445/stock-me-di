package `in`.kdi.integration_tests

import `in`.kdi.compiler.core.test.Function
import `in`.kdi.compiler.core.test.ProjectCompilationException
import `in`.kdi.compiler.core.test.ProjectCompiler
import `in`.kdi.compiler.ksp.DIProcessor
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import me.tatarka.inject.compiler.ksp.InjectProcessorProvider

class DiModuleTest : FreeSpec({
	"Module must be object class" {
		shouldThrowExactly<ProjectCompilationException> {
			ProjectCompiler(
				workingDir = tempdir()
			).symbolProcessor(DIProcessor.Provider())
				.symbolProcessor(InjectProcessorProvider())
				.source(
					fileName = "Main.kt",
					source = """
                package main
                import `in`.kdi.runtime.annotations.Module
                import `in`.kdi.runtime.annotations.Component
                @Module
                abstract class Main

            """.trimIndent()
				).compile()
		}.message?.contains("Module must be a object class") shouldBe true
	}

	"Module installing in component" {
		ProjectCompiler()
			.symbolProcessor(DIProcessor.Provider())
			.symbolProcessor(InjectProcessorProvider())
			.source(
				fileName = "Main.kt",
				source = """
					package main
					import `in`.kdi.runtime.annotations.Module
					import `in`.kdi.runtime.annotations.Component
					import `in`.kdi.runtime.annotations.InstallIn
					import me.tatarka.inject.annotations.Provides
					import me.tatarka.inject.annotations.Scope
					import `in`.kdi.runtime.annotations.AssociatedWith

					@Module
					@InstallIn(Comp1::class)
					@Comp1Scope
					object Comp1Module {
					    
					    @Provides
					    fun provideA() = A()
					}

					class A

					@Scope
					@AssociatedWith(Comp1::class)
					annotation class Comp1Scope


					@Component
					@Comp1Scope
					abstract class Comp1 {
					 abstract val a: A
					}
				""".trimIndent()
			).compile()
	}

	"Multi-gradle-module di module installation" {
		val subProject = ProjectCompiler()
			.symbolProcessor(DIProcessor.Provider())
			.symbolProcessor(InjectProcessorProvider())

		val mainProject = ProjectCompiler(
			dependencies = listOf(subProject)
		)
			.symbolProcessor(DIProcessor.Provider())
			.symbolProcessor(InjectProcessorProvider())

		subProject.source(
			fileName = "Sub.kt",
			source = """
				package com.sub
                import `in`.kdi.runtime.Singleton
                import `in`.kdi.runtime.SingletonComponent
                import `in`.kdi.runtime.annotations.InstallIn
                import `in`.kdi.runtime.annotations.Module
                import me.tatarka.inject.annotations.Provides
                import `in`.kdi.runtime.annotations.Component

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
                import com.sub.create
                import com.sub.DepA
                import `in`.kdi.runtime.annotations.Component
                import `in`.kdi.runtime.SingletonComponent
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
                val comp = Science::class.create(SubjectComponent::class.create(SingletonComponent.getInstance())) 
                println(comp.depB)
                }

            """.trimIndent()
		)

		mainProject.compile().runStaticFunction(
			function = Function(
                className = "com.dev.MainKt",
                functionName = "main",
                args = listOf()
            )
		)
	}
})