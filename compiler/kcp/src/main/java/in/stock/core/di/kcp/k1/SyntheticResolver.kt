package `in`.stock.core.di.kcp.k1

import `in`.stock.core.di.kcp.utils.FqNames
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.*
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.descriptorUtil.classId
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import org.jetbrains.kotlin.resolve.extensions.SyntheticResolveExtension
import org.jetbrains.kotlin.types.KotlinType

// todo find an approach to generate getter/setter for annotated val property or user lateinit var approach
open class SyntheticResolver : SyntheticResolveExtension {

	companion object {

		private val GenerateConstructor = Name.identifier("generateConstructor")
		private val ComponentProperty = Name.identifier("component")
	}

	override fun getSyntheticNestedClassNames(thisDescriptor: ClassDescriptor): List<Name> {
		return if (thisDescriptor.annotations.hasAnnotation(FqNames.EntryPoint)) {
			listOf(
				GenerateConstructor
			)
		} else {
			emptyList()
		}
	}

	override fun generateSyntheticSecondaryConstructors(
		thisDescriptor: ClassDescriptor,
		bindingContext: BindingContext,
		result: MutableCollection<ClassConstructorDescriptor>
	) {
		if (thisDescriptor.annotations.hasAnnotation(FqNames.EntryPoint)) {
			val entryPointAnnotation = thisDescriptor.annotations.findAnnotation(FqNames.EntryPoint)!!
			when (entryPointAnnotation.allValueArguments[Name.identifier("initializer")]?.value) {
				"constructor", null -> {
					generateComponentConstructor(thisDescriptor, result)
				}

				else -> {
					// todo add support for component initialization other than constructor
				}
			}
		}
	}

	private fun generateComponentConstructor(
		classDescriptor: ClassDescriptor,

		result: MutableCollection<ClassConstructorDescriptor>
	) {
		val componentClass = classDescriptor.classId?.let {
			classDescriptor.module.findClassAcrossModuleDependencies(
				ClassId(it.packageFqName, Name.identifier("${classDescriptor.name.asString()}Component"))
			)
		} ?: return

		val constructor = ClassConstructorDescriptorImpl.createSynthesized(
			classDescriptor,
			Annotations.create(listOf()),
			false,
			SourceElement.NO_SOURCE
		)

		val componentParameters = listOf(
			ValueParameterDescriptorImpl(
				containingDeclaration = constructor,
				original = null,
				index = 0,
				annotations = Annotations.create(listOf()),
				name = Name.identifier("component"),
				outType = componentClass.defaultType,
				declaresDefaultValue = false,
				isCrossinline = false,
				isNoinline = false,
				varargElementType = null,
				source = SourceElement.NO_SOURCE
			)
		)

		constructor.initialize(
			componentParameters,
			DescriptorVisibilities.DEFAULT_VISIBILITY,
		)

		constructor.returnType = classDescriptor.defaultType

		result.add(constructor)
	}

	override fun generateSyntheticProperties(
		thisDescriptor: ClassDescriptor,
		name: Name,
		bindingContext: BindingContext,
		fromSupertypes: ArrayList<PropertyDescriptor>,
		result: MutableSet<PropertyDescriptor>
	) {
		when {
			name == ComponentProperty -> {
				val componentType = thisDescriptor.classId?.let {
					thisDescriptor.module.findClassAcrossModuleDependencies(
						ClassId(it.packageFqName, Name.identifier("${thisDescriptor.name.asString()}Component"))
					)
				} ?: return

				val property = thisDescriptor.generateComponentProperty(
					name,
					componentType.defaultType,
					typeParameters = listOf()
				)
				result.add(property)
			}

			name.asString().contains("-generate") -> {
				error("$name")
			}
		}
	}

	override fun getSyntheticPropertiesNames(thisDescriptor: ClassDescriptor): List<Name> {
		return if (thisDescriptor.annotations.hasAnnotation(FqNames.EntryPoint)) {
			listOf(ComponentProperty)
		} else {
			emptyList()
		}
	}

	private fun ClassDescriptor.generateComponentProperty(
		name: Name,
		type: KotlinType,
		typeParameters: List<TypeParameterDescriptor>
	): PropertyDescriptor {
		val propertyDescriptor = PropertyDescriptorImpl.create(
			this, Annotations.create(listOfNotNull()), modality, visibility, false, name,
			CallableMemberDescriptor.Kind.SYNTHESIZED, source, false, false, false, false, false, false
		)

		val extensionReceiverParameter: ReceiverParameterDescriptor? = null // kludge to disambiguate call
		propertyDescriptor.setType(
			type,
			typeParameters,
			thisAsReceiverParameter,
			extensionReceiverParameter,
			emptyList()
		)

		val propertyGetter = PropertyGetterDescriptorImpl(
			propertyDescriptor, Annotations.create(listOfNotNull()), modality, visibility, false, false, false,
			CallableMemberDescriptor.Kind.SYNTHESIZED, null, source
		)

		propertyGetter.initialize(type)

		val propertySetter = PropertySetterDescriptorImpl(
			propertyDescriptor, Annotations.create(listOfNotNull()), modality, visibility, false, false, false,
			CallableMemberDescriptor.Kind.SYNTHESIZED, null, source
		)

		propertySetter.initialize(
			ValueParameterDescriptorImpl(
				propertyDescriptor, null, 0, Annotations.create(listOfNotNull()), Name.identifier("value"), type, false, false,
				false, null, source
			)
		)

		val backingField = FieldDescriptorImpl(Annotations.create(listOfNotNull()), propertyDescriptor)

		propertyDescriptor.initialize(propertyGetter, propertySetter, backingField, null)

		return propertyDescriptor
	}
}
