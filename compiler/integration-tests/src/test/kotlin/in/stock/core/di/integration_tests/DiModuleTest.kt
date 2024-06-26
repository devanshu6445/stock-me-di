package `in`.stock.core.di.integration_tests

import `in`.stock.core.di.compiler.core.test.Function
import `in`.stock.core.di.compiler.core.test.ProjectCompilationException
import `in`.stock.core.di.compiler.core.test.ProjectCompiler
import `in`.stock.core.di.compiler.ksp.ModuleProcessor
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
			).symbolProcessor(ModuleProcessor.Provider())
				.symbolProcessor(InjectProcessorProvider())
				.source(
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
			.symbolProcessor(ModuleProcessor.Provider())
			.symbolProcessor(InjectProcessorProvider())
			.source(
				fileName = "Main.kt",
				source = """
					package main
					import `in`.stock.core.di.runtime.annotations.Module
					import `in`.stock.core.di.runtime.annotations.Component
					import `in`.stock.core.di.runtime.annotations.InstallIn
					import me.tatarka.inject.annotations.Provides
					import me.tatarka.inject.annotations.Scope
					import `in`.stock.core.di.runtime.annotations.AssociatedWith

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
			.symbolProcessor(ModuleProcessor.Provider())
			.symbolProcessor(InjectProcessorProvider())

		val mainProject = ProjectCompiler(
			dependencies = listOf(subProject)
		)
			.symbolProcessor(ModuleProcessor.Provider())
			.symbolProcessor(InjectProcessorProvider())

		subProject.source(
			fileName = "Sub.kt",
			source = """
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

		mainProject.compile().runStaticFunction(
			function = Function(
				className = "com.dev.MainKt",
				functionName = "main",
				args = listOf()
			)
		)
	}
})