package `in`.kdi.compiler.ksp.generators

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile
import com.squareup.kotlinpoet.ksp.toAnnotationSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import `in`.kdi.compiler.core.Generator
import `in`.kdi.compiler.core.XCodeGenerator
import `in`.kdi.compiler.core.ext.writeTo
import `in`.kdi.compiler.ksp.data.ModuleInfo
import `in`.kdi.compiler.ksp.data.ModuleProviderResult
import `in`.kdi.compiler.ksp.data.ProvidesInfo
import `in`.kdi.runtime.annotations.internals.ModuleProvider
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
		addFunctions(
		    providers.map { provider ->
			if (provider.isCollectedIntoMap) {
				createMapProvider(
					provider = provider
				)
			} else {
				addProviderBinder(
					providesInfo = provider
				)
			}
		}
		)
	}

	private fun createMapProvider(provider: ProvidesInfo): FunSpec {
		return FunSpec.builder(provider.functionName.asString())
			.returns(provider.reference.returnType?.toTypeName() ?: error("No return type"))
			.addAnnotations(
				provider.reference.annotations.map {
					it.toAnnotationSpec()
				}.toList()
			)
			.addParameters(
			    provider.reference.parameters.map { param ->
				ParameterSpec.builder(
					name = param.name?.asString() ?: error("No name value parameter"),
					type = param.type.toTypeName(),
				).build()
			}
			)
			.addCode(
				CodeBlock.of(
					"""
								return %T.${provider.reference.simpleName.asString()}(
								${
						buildString {
							provider.reference.parameters.forEach {
								append(it.name?.asString())
								append(" = ")
								append(it.name?.asString())
							}
						}
					}
								)
							""".trimIndent(),
					(provider.reference.parentDeclaration as KSClassDeclaration).toClassName()
				)
			)
			.build()
	}

	private fun addProviderBinder(providesInfo: ProvidesInfo) =
		FunSpec.builder("bind${providesInfo.providerName.simpleName}For${providesInfo.reference.simpleName.asString()}")
			.receiver(receiverType = providesInfo.providerName)
			.addAnnotations(providesInfo.reference.annotations.map { it.toAnnotationSpec() }.toList())
			.returns(providesInfo.reference.returnType!!.toTypeName())
			.addStatement("return instance")
			.build()
}