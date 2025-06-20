package `in`.kdi.runtime.android

import `in`.kdi.runtime.SingletonComponent
import `in`.kdi.runtime.annotations.Component
import me.tatarka.inject.annotations.Provides
import me.tatarka.inject.annotations.Scope

@Component
@ActivityRetainedScope
abstract class ActivityRetainedComponent(
	@Component
	@get:Provides
	val singletonComponent: SingletonComponent
)

@Scope
annotation class ActivityRetainedScope