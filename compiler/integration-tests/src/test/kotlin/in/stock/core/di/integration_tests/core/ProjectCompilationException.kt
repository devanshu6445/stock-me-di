package `in`.stock.core.di.integration_tests.core

class ProjectCompilationException(diagnosticInfo: String) : IllegalStateException(
  p0 = "Project could not be compiled. Diagnostic Info --> $diagnosticInfo",
)