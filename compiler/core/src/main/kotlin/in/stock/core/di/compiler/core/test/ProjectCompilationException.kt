package `in`.stock.core.di.compiler.core.test

class ProjectCompilationException(diagnosticInfo: String) : IllegalStateException(
	"Project could not be compiled. Diagnostic Info --> $diagnosticInfo"
)
