package `in`.stock.core.di.kcp.backend.core

import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin

object Origin : IrDeclarationOrigin {
	override val name: String
		get() = "stock-me-di"
}