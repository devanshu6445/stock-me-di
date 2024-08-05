package `in`.stock.core.di.compiler.ksp.utils

import com.squareup.kotlinpoet.*
import kotlin.reflect.KClass

fun valProperty(name: String, type: TypeName) = PropertySpec.builder(name, type)
  .build()

fun valProperty(name: String, type: KClass<*>, vararg annotation: ClassName): PropertySpec {
  return PropertySpec.builder(name, type, KModifier.PRIVATE)
    .apply {
      annotation.forEach {
        addAnnotation(it)
      }
    }
    .build()
}

fun valProperty(name: String, type: TypeName, vararg annotation: ClassName): PropertySpec {
  return PropertySpec.builder(name, type)
    .apply {
      annotation.forEach {
        addAnnotation(it)
      }
    }
    .build()
}

fun FunSpec.Builder.addConstructorProperty(
	typeSpec: TypeSpec.Builder,
	name: String,
	type: TypeName,
	annotations: List<AnnotationSpec> = emptyList(),
	addInitializer: Boolean = true
) = apply {
  addParameter(ParameterSpec.builder(name, type).addAnnotations(annotations).build())
  typeSpec.addProperty(
    PropertySpec.builder(name, type)
      .apply {
        if (addInitializer) {
            initializer(name)
        }
      }
      .build()
  )
}