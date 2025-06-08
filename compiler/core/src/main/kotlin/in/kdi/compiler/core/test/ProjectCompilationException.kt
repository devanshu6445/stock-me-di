package `in`.kdi.compiler.core.test

class ProjectCompilationException(diagnosticInfo: String) : IllegalStateException(
	"Project could not be compiled. Diagnostic Info --> $diagnosticInfo"
)
