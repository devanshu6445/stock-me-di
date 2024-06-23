package `in`.stock.core.di.integration_tests.core

sealed interface Target {
	data object KSP : Target
}