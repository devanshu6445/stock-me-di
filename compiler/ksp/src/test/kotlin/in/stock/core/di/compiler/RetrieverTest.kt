package `in`.stock.core.di.compiler

import `in`.stock.core.di.compiler.core.test.Function
import `in`.stock.core.di.compiler.core.test.ProjectCompiler
import `in`.stock.core.di.compiler.ksp.DIProcessor
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.core.spec.style.FreeSpec
import me.tatarka.inject.compiler.ksp.InjectProcessorProvider

class RetrieverTest : FreeSpec({
	lateinit var compiler: ProjectCompiler

	beforeTest {
		compiler = ProjectCompiler()
			.symbolProcessor(DIProcessor.Provider())
			.symbolProcessor(InjectProcessorProvider())
	}

	"component_should_be_casted_to_retriever" {

		compiler.source(
			fileName = "src/testData/initializer_entry_point_generated_component_should_be_casted_to_retriever.kt"
		)

		compiler.compile()
			.apply {
				shouldNotThrow<IllegalStateException> {
					runStaticFunction(
						function = Function(
							className = "main.Initializer_entry_point_generated_component_should_be_casted_to_retrieverKt",
							functionName = "main",
							args = emptyList()
						)
					)
				}
			}
	}

	"retriever_should_be_able_to_provide_component_as_dep" {
		compiler.source(
			fileName = "src/testData/retriever_should_be_able_to_provide_component_as_dependency.kt"
		).compile()
			.runStaticFunction(
				function = Function(
					className = "main.Retriever_should_be_able_to_provide_component_as_dependencyKt",
					functionName = "main",
					args = emptyList()
				)
			)
	}
})