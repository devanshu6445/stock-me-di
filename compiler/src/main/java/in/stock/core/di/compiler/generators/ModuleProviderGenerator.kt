package `in`.stock.core.di.compiler.generators

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName
import `in`.stock.core.di.compiler.core.FlexibleCodeGenerator
import `in`.stock.core.di.compiler.core.Generator
import `in`.stock.core.di.compiler.core.writeTo
import `in`.stock.core.di.compiler.data.ModuleInfo
import `in`.stock.core.di.compiler.data.ModuleProviderResult
import `in`.stock.core.di.compiler.data.ProvidesInfo
import `in`.stock.core.di.compiler.utils.Provides
import `in`.stock.core.di.runtime.annotations.internals.ModuleProvider
import javax.inject.Inject

class ModuleProviderGenerator @Inject constructor(
    private val codeGenerator: FlexibleCodeGenerator,
) : Generator<ModuleInfo, ModuleProviderResult> {
    override fun generate(data: ModuleInfo): ModuleProviderResult {

        val moduleName = data.root.toClassName().let {
            ClassName(it.packageName, "${it.simpleName}Provider")
        }
        FileSpec.builder(moduleName)
            .createProvider(
                moduleName = moduleName,
                providers = data.providers,
                installInComponent = data.installInComponent
            )
            .build()
            .writeTo(codeGenerator)

        return ModuleProviderResult(
            name = moduleName,
            installingComponent = data.installInComponent
        )
    }

    private fun FileSpec.Builder.createProvider(
        moduleName: ClassName,
        providers: List<ProvidesInfo>,
        installInComponent: KSClassDeclaration
    ) = apply {
        addType(
            TypeSpec.interfaceBuilder(moduleName)
                .addAnnotation(
                    AnnotationSpec.builder(ModuleProvider::class.asClassName())
                        .addMember(
                            "%L = %T::class",
                            "clazz",
                            installInComponent.toClassName()
                        )
                        .build()
                )
                .addProviderBinders(providers)
                .build()
        )
    }

    private fun TypeSpec.Builder.addProviderBinders(providers: List<ProvidesInfo>) = apply {
        addFunctions(providers.map { addProviderBinder(it) })
    }

    private fun addProviderBinder(providesInfo: ProvidesInfo) =
        FunSpec.builder("bind" + providesInfo.providerName.simpleName)
            .receiver(receiverType = providesInfo.providerName)
            .addAnnotation(Provides)
            .returns(providesInfo.resolvedDepType.toClassName())
            .addStatement("return instance")
            .build()
}