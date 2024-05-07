package `in`.stock.core.di.integration_tests

import `in`.stock.core.di.runtime.Singleton
import `in`.stock.core.di.runtime.SingletonComponent
import `in`.stock.core.di.runtime.annotations.InstallIn
import `in`.stock.core.di.runtime.annotations.Module
import me.tatarka.inject.annotations.Provides

@Module
@InstallIn(SingletonComponent::class)
@Singleton
object Module1 {

  @Provides
  fun bind(
  ): Dep {
    return Dep()
  }

  @Provides
  fun bindA(): DepB {
    return DepB()
  }
}

@Module
@InstallIn(NewComponent::class)
@NewScope
object Module2 {

  @Provides
  fun bind(dep: Dep): Dep2 {
    return Dep2(dep)
  }

  @Provides
  fun bindA(): Dep3 {
    return Dep3()
  }
}

class Dep

class DepB

class Dep2(private val dep: Dep)

class Dep3 {
  private val a: Dep
    get() = TestComponent::class.create(
      dep3 = this
    ).dep
}