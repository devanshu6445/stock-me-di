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

  println(entryPoint::class.java.declaredFields.first { it.name == "aDelegate" }.get(entryPoint))
}

@Inject
@Singleton
class A

class C

@Module
@InstallIn(SingletonComponent::class)
@Singleton
object Module {

  @Provides
  fun provide() = C()
}