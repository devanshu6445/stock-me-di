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

  // Try adding program arguments via Run/Debug configuration.
  // Learn more about running applications: https://www.jetbrains.cop-m/help/idea/running-applications.html.
  println("Program arguments: ${args.joinToString()}")
  println(B(component = BComponent::class.create()).ab.toString())
  println("L")
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