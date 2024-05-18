package `in`.stock.core.di.integration_tests

import `in`.stock.core.di.runtime.Singleton
import `in`.stock.core.di.runtime.SingletonComponent
import `in`.stock.core.di.runtime.annotations.EntryPoint
import `in`.stock.core.di.runtime.annotations.InstallIn
import `in`.stock.core.di.runtime.annotations.Module
import me.tatarka.inject.annotations.Inject
import me.tatarka.inject.annotations.Provides

fun main(args: Array<String>) {
  println("Hello Wold!")

  val entryPoint = EntryPointTest(component = EntryPointTestComponent::class.create())

  println(entryPoint.a)
}

@Inject
@Singleton
class A

@EntryPoint
class B(val ab: A)

class C

@Module
@InstallIn(SingletonComponent::class)
@Singleton
object Module {

  @Provides
  fun provide() = C()
}


class AB(val b: A) {

  @`in`.stock.core.di.runtime.annotations.Inject
  private lateinit var a : A

  fun b() {
    a.toString()
  }
}