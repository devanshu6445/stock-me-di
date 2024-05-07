package `in`.stock.core.di.kotlin_di_compiler.k1

import `in`.stock.core.di.kotlin_di_compiler.utils.FqNames
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.ClassConstructorDescriptorImpl
import org.jetbrains.kotlin.descriptors.impl.ValueParameterDescriptorImpl
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.descriptorUtil.classId
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import org.jetbrains.kotlin.resolve.extensions.SyntheticResolveExtension

open class SyntheticResolver : SyntheticResolveExtension {

  companion object {

    private val GenerateConstructor = Name.identifier("generateConstructor")
  }

  override fun getSyntheticNestedClassNames(thisDescriptor: ClassDescriptor): List<Name> {
    return if (thisDescriptor.annotations.hasAnnotation(FqNames.EntryPoint)) listOf(
      GenerateConstructor
    ) else emptyList()
  }

  override fun generateSyntheticSecondaryConstructors(
    thisDescriptor: ClassDescriptor,
    bindingContext: BindingContext,
    result: MutableCollection<ClassConstructorDescriptor>
  ) {
    if (thisDescriptor.annotations.hasAnnotation(FqNames.EntryPoint)) {
      generateComponentConstructor(thisDescriptor, result)
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

//        val primaryConstructor = classDescriptor.constructors.find { it.isPrimary }

    val componentParameter = ValueParameterDescriptorImpl(
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

    constructor.initialize(
      listOf(componentParameter),
      DescriptorVisibilities.DEFAULT_VISIBILITY,
    )

    constructor.returnType = classDescriptor.defaultType

    result.add(constructor)
  }
}