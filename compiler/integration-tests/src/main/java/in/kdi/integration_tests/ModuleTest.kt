package `in`.kdi.integration_tests

import `in`.kdi.runtime.Singleton
import `in`.kdi.runtime.SingletonComponent
import `in`.kdi.runtime.annotations.InstallIn
import `in`.kdi.runtime.annotations.Module
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