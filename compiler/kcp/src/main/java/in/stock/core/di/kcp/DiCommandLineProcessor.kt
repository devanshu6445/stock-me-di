package `in`.stock.core.di.kcp

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

const val CompilerPluginId = "stock-me-di-compiler"

@OptIn(ExperimentalCompilerApi::class)
class DiCommandLineProcessor : CommandLineProcessor {
  override val pluginId: String
    get() = CompilerPluginId
  override val pluginOptions: Collection<AbstractCliOption>
    get() = listOf(
      CliOption(
        optionName = "enabled",
        valueDescription = "enabled",
        description = "enabled",
        required = false
      )
    )

  override fun processOption(
    option: AbstractCliOption,
    value: String,
    configuration: CompilerConfiguration
  ) {
    when (option.optionName) {
      "enabled" -> configuration.put(ARG_ENABLED, value)
    }
  }

  companion object {
    val ARG_ENABLED = CompilerConfigurationKey<String>("enabled")
  }
}