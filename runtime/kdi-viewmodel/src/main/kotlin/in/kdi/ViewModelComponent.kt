package `in`.kdi

import androidx.lifecycle.ViewModel
import `in`.stock.core.di.runtime.android.ActivityComponent
import `in`.stock.core.di.runtime.android.ActivityRetainedComponent
import `in`.stock.core.di.runtime.annotations.Component
import `in`.stock.core.di.runtime.annotations.Retriever
import `in`.stock.core.di.runtime.components.Provider
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