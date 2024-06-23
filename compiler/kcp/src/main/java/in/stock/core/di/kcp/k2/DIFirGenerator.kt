package `in`.stock.core.di.kcp.k2

import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar

class DIFirGenerator(
  private val messageCollector: MessageCollector
) : FirExtensionRegistrar() {
  override fun ExtensionRegistrarContext.configurePlugin() {
    +::FirDeclarationGenerator.bind(messageCollector)
    +::PredicateMatcher
  }
}
