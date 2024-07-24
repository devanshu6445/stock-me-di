package `in`.stock.core.di.compiler.ksp.generators

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import `in`.stock.core.di.compiler.core.Generator
import `in`.stock.core.di.compiler.core.XCodeGenerator
import `in`.stock.core.di.compiler.core.ext.writeTo
import `in`.stock.core.di.compiler.ksp.data.ModuleProviderResult
import `in`.stock.core.di.compiler.ksp.utils.ModuleProviderRegistry
import `in`.stock.core.di.runtime.annotations.internals.Registry
import javax.inject.Inject

class ModuleProviderRegistryGenerator @Inject constructor(
	private val xCodeGenerator: XCodeGenerator
) : Generator<List<@JvmSuppressWildcards ModuleProviderResult>, Unit> {

  override fun generate(data: List<ModuleProviderResult>) {
    val properties = data.map {
      PropertySpec.builder(
        name = it.name.simpleName,
        type = String::class
      ).addModifiers(KModifier.CONST)
        .initializer(format = "%S", it.name.canonicalName)
        .build()
    }
    FileSpec.builder(ModuleProviderRegistry)
      .addType(
        TypeSpec.objectBuilder(ModuleProviderRegistry)
          .addAnnotation(Registry::class)
          .addProperties(properties)
          .build()
      )
			.build().writeTo(xCodeGenerator)
  }
}