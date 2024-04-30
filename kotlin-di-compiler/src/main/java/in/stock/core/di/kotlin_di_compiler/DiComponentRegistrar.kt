package `in`.stock.core.di.kotlin_di_compiler

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter

class DiComponentRegistrar : CompilerPluginRegistrar() {
    override val supportsK2: Boolean = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        val messageCollector =
            configuration.get(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)
        val enabled = configuration.get(DiCommandLineProcessor.ARG_ENABLED, "true").toBoolean()

        FirExtensionRegistrarAdapter.registerExtension(DIFirGenerator(messageCollector))
        IrGenerationExtension.registerExtension(DiIrGenerator(messageCollector = messageCollector))
    }
}