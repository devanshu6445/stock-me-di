package `in`.kdi.core.di.runtime.android

import `in`.stock.core.di.runtime.annotations.Component
import `in`.stock.core.di.runtime.annotations.Retriever
import me.tatarka.inject.annotations.Provides
import me.tatarka.inject.annotations.Scope

@Component
@ActivityScope
abstract class ActivityComponent(
    @Component
    @get:Provides
    val activityRetainedComponent: ActivityRetainedComponent
)

@Scope
annotation class ActivityScope

@Retriever(component = ActivityComponent::class)
interface ActivityEntryPoint {
    val activityComponent: ActivityComponent
}