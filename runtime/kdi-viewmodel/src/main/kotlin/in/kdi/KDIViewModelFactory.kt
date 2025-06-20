package `in`.kdi

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import `in`.kdi.runtime.RetrieverGetter

@Suppress("UNCHECKED_CAST")
class KDIViewModelFactory(
	private val activity: Activity
) : ViewModelProvider.Factory {
	override fun <T : ViewModel> create(modelClass: Class<T>): T {
		val viewModelFactoriesRetriever = RetrieverGetter.get(
			obj = activity,
			retriever = ViewModelFactoriesRetriever::class
		)

		val viewModelBuilder = viewModelFactoriesRetriever.viewModelProviderMap[modelClass.kotlin]
			?: throw IllegalArgumentException("Unknown ViewModel class: $modelClass")

		return viewModelBuilder.get() as T
	}
}