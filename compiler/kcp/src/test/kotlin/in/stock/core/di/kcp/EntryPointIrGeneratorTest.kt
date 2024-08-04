package `in`.stock.core.di.kcp

import `in`.stock.core.di.compiler.core.test.Function
import `in`.stock.core.di.compiler.core.test.ProjectCompiler
import `in`.stock.core.di.compiler.ksp.DIProcessor
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.engine.spec.tempdir
import me.tatarka.inject.compiler.ksp.InjectProcessorProvider

class EntryPointIrGeneratorTest : BehaviorSpec({

	lateinit var projectCompiler: ProjectCompiler

	beforeTest {
		projectCompiler = ProjectCompiler(
			workingDir = tempdir()
		)
			.symbolProcessor(DIProcessor.Provider())
			.symbolProcessor(InjectProcessorProvider())
			.compilerPlugin(DiComponentRegistrar())
			.commandLineProcessor(DiCommandLineProcessor())
	}

	given("An @EntryPoint marked class") {

		`when`("constructor is provided as argument for initializer") {

			then("create a secondary constructor") {
				projectCompiler.source(
					fileName = "src/testData/secondary_constructor_test.kt",
				).compile().apply {
					runStaticFunction(
						Function(
							className = "main.Secondary_constructor_testKt",
							functionName = "main",
							args = emptyList()
						)
					)
				}
			}
		}
	}
})
