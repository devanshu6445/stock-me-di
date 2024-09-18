package `in`.stock.core.di.compiler.ksp.generators

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile
import com.squareup.kotlinpoet.ksp.toAnnotationSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import `in`.stock.core.di.compiler.core.Generator
import `in`.stock.core.di.compiler.core.XCodeGenerator
import `in`.stock.core.di.compiler.core.ext.writeTo
import `in`.stock.core.di.compiler.ksp.data.ModuleInfo
import `in`.stock.core.di.compiler.ksp.data.ModuleProviderResult
import `in`.stock.core.di.compiler.ksp.data.ProvidesInfo
import `in`.stock.core.di.runtime.annotations.internals.ModuleProvider
import javax.inject.Inject

class ModuleProviderGenerator @Inject constructor(
	private val xCodeGenerator: XCodeGenerator,
) : Generator<ModuleInfo, ModuleProviderResult> {
	override fun generate(data: ModuleInfo): ModuleProviderResult {
		val moduleName = data.root.toClassName().let {
			ClassName(it.packageName, "${it.simpleName}Provider")
		}
		FileSpec.builder(moduleName)
			.createProvider(
				moduleName = moduleName,
				providers = data.providers,
				installInComponent = data.installInComponent,
				originatingFile = data.root.containingFile
			)
			.build()
			.writeTo(xCodeGenerator)

		return ModuleProviderResult(
			name = moduleName,
			installingComponent = data.installInComponent
		)
	}

	private fun FileSpec.Builder.createProvider(
		moduleName: ClassName,
		providers: List<ProvidesInfo>,
		installInComponent: KSClassDeclaration,
		originatingFile: KSFile?
	) = apply {
		addType(
			TypeSpec.interfaceBuilder(moduleName)
				.apply {
					if (originatingFile != null) {
						addOriginatingKSFile(originatingFile)
					}
				}
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
		FunSpec.builder("bind${providesInfo.providerName.simpleName}For${providesInfo.reference.simpleName.asString()}")
			.receiver(receiverType = providesInfo.providerName)
			.addAnnotations(providesInfo.reference.annotations.map { it.toAnnotationSpec() }.toList())
			.returns(providesInfo.reference.returnType!!.toTypeName())
			.addStatement("return instance")
			.build()
}