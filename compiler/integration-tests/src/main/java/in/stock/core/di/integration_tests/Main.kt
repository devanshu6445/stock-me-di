package `in`.stock.core.di.integration_tests

import `in`.stock.core.di.runtime.Singleton
import `in`.stock.core.di.runtime.SingletonComponent
import `in`.stock.core.di.runtime.annotations.InstallIn
import `in`.stock.core.di.runtime.annotations.Module
import me.tatarka.inject.annotations.Inject
import me.tatarka.inject.annotations.Provides

fun main(args: Array<String>) {
  println("Hello Wold!")

  val entryPoint = EntryPointTest(component = EntryPointTestComponent::class.create())

  val delegateField = entryPoint::class.java.declaredFields.first { it.name == "aDelegate" }
  println((delegateField.get(entryPoint) as Lazy<*>).value)
  println(entryPoint.aLazy.value)

  println(entryPoint.component.aLazy)
}