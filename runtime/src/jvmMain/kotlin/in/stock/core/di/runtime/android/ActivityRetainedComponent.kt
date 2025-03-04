package `in`.stock.core.di.runtime.android

import `in`.stock.core.di.runtime.SingletonComponent
import `in`.stock.core.di.runtime.annotations.Component
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