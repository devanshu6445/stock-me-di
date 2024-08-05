package `in`.stock.core.di.compiler.core.test

class ProjectCompilationException(diagnosticInfo: String) : IllegalStateException(
	p0 = "Project could not be compiled. Diagnostic Info --> $diagnosticInfo"
)
