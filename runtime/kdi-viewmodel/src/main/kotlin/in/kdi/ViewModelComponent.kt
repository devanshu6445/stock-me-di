package `in`.kdi

import androidx.lifecycle.ViewModel
import `in`.kdi.runtime.android.ActivityComponent
import `in`.kdi.runtime.android.ActivityRetainedComponent
import `in`.kdi.runtime.annotations.Component
import `in`.kdi.runtime.annotations.Retriever
import `in`.kdi.runtime.components.Provider
import me.tatarka.inject.annotations.Provides
import me.tatarka.inject.annotations.Scope
import kotlin.reflect.KClass

@Component
@ViewModelScope
abstract class ViewModelComponent(
	@Component
	@get:Provides
	val activityRetainedComponent: ActivityRetainedComponent
)

@Retriever(component = ActivityComponent::class)
interface ViewModelFactoriesRetriever {
	val viewModelProviderMap: Map<KClass<*>, Provider<ViewModel>>
}

@Scope
annotation class ViewModelScope