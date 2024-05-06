package `in`.stock.core.di.kotlin_di_compiler

import `in`.stock.core.di.kotlin_di_compiler.backend.DiIrGenerator
import `in`.stock.core.di.kotlin_di_compiler.k1.SyntheticResolver
import `in`.stock.core.di.kotlin_di_compiler.k2.DIFirGenerator
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter
import org.jetbrains.kotlin.resolve.extensions.SyntheticResolveExtension

class DiComponentRegistrar : CompilerPluginRegistrar() {
    override val supportsK2: Boolean = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        val messageCollector =
            configuration.get(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)
//        val enabled = configuration.get(DiCommandLineProcessor.ARG_ENABLED, "true").toBoolean()

        SyntheticResolveExtension.registerExtension(SyntheticResolver())

        FirExtensionRegistrarAdapter.registerExtension(DIFirGenerator(messageCollector))
        IrGenerationExtension.registerExtension(DiIrGenerator(messageCollector = messageCollector))
    }
}