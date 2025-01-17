package `in`.stock.core.di.compiler.ksp.generators

import `in`.stock.core.di.compiler.core.Generator
import `in`.stock.core.di.compiler.ksp.data.ModuleInfo
import `in`.stock.core.di.compiler.ksp.data.ProvidesInfo
import javax.inject.Inject

class ModuleGenerator @Inject constructor(
  private val providerGenerator: Generator<ProvidesInfo, Unit>,
) : Generator<ModuleInfo, Unit> {

  override fun generate(data: ModuleInfo) {
    for (provider in data.providers) {
      providerGenerator.generate(
        data = provider
      )
    }
  }
}