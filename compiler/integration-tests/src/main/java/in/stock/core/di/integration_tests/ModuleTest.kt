package `in`.stock.core.di.integration_tests

import `in`.stock.core.di.runtime.Singleton
import `in`.stock.core.di.runtime.SingletonComponent
import `in`.stock.core.di.runtime.annotations.InstallIn
import `in`.stock.core.di.runtime.annotations.Module
import me.tatarka.inject.annotations.Inject
import me.tatarka.inject.annotations.Provides

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

@Inject
class B