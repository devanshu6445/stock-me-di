package `in`.stock.core.di.integration_tests

fun main(args: Array<String>) {
  println("Hello Wold!")

  val entryPoint = EntryPointTest(component = EntryPointTestComponent::class.create())

  println(entryPoint.aLazy.value)

  println(entryPoint.component.aLazy)

	PrimaryConstructorEntryPoint(
		component = PrimaryConstructorEntryPointComponent::class.create()
	).apply {
		println(dep)
		println(b.value)
	}
}