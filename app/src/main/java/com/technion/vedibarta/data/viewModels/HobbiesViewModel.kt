package com.technion.vedibarta.data.viewModels

import android.content.Context
import androidx.lifecycle.*
import com.technion.vedibarta.POJOs.*
import com.technion.vedibarta.data.loadHobbies
import com.technion.vedibarta.utilities.extensions.handleError
import com.technion.vedibarta.utilities.extensions.handleSuccess
import com.technion.vedibarta.utilities.extensions.handleTimeout
import com.technion.vedibarta.utilities.resourcesManagement.MultilingualResource
import com.technion.vedibarta.utilities.resourcesManagement.RemoteResourcesManager
import java.lang.Class
import java.lang.IllegalArgumentException

fun hobbiesViewModelFactory(context: Context) = object : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(HobbiesViewModel::class.java)) HobbiesViewModel(
            context
        ) as T else throw IllegalArgumentException()
    }
}

class HobbiesViewModel(private val context: Context) : ViewModel() {

    private val _hobbiesResources =
        MutableLiveData<LoadableData<HobbiesResources>>(NormalLoading())

    val hobbiesResources: LiveData<LoadableData<HobbiesResources>> = _hobbiesResources

    val chosenHobbies = mutableListOf<String>()

    private var startedLoading = false

    fun startLoading() {
        if (!startedLoading) {
            val resourcesManager = RemoteResourcesManager(context)
            getHobbiesResources(resourcesManager, _hobbiesResources)
            startedLoading = true
        }
    }

    private fun getHobbiesResources(
        resourcesManager: RemoteResourcesManager,
        into: MutableLiveData<LoadableData<HobbiesResources>>
    ) {
        val task1 = resourcesManager.findMultilingualResource("hobbies/all")
        val task2 = loadHobbies(context)

        task1.run {

            continueWithTask { allTask ->
                val allHobbies = allTask.result!!

                task2.continueWith { mapperTask ->
                    val hobbiesCardList = mapperTask.result!!

                    HobbiesResources(allHobbies, hobbiesCardList)
                }
            }
                .handleSuccess(into)
                .handleError(into)
                .handleTimeout(into)
        }
    }

    data class HobbiesResources(
        val allHobbies: MultilingualResource,
        val hobbyCardList: List<CategoryCard>
    )

}
