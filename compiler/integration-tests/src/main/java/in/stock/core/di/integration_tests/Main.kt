package `in`.stock.core.di.integration_tests

fun main(args: Array<String>) {
  println("Hello Wold!")

  val entryPoint = EntryPointTest(component = EntryPointTestComponent::class.create())

  val delegateField = entryPoint::class.java.declaredFields.first { it.name == "aDelegate" }
  println((delegateField.get(entryPoint) as Lazy<*>).value)
  println(entryPoint.aLazy.value)

  println(entryPoint.component.aLazy)
}