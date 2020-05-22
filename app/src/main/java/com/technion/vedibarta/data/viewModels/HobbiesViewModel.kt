package com.technion.vedibarta.data.viewModels

import android.content.Context
import androidx.lifecycle.*
import com.technion.vedibarta.POJOs.*
import com.technion.vedibarta.data.loadHobbies
import com.technion.vedibarta.utilities.extensions.handleError
import com.technion.vedibarta.utilities.extensions.handleSuccess
import com.technion.vedibarta.utilities.extensions.handleTimeout
import com.technion.vedibarta.utilities.resourcesManagement.MultilingualTextResource
import com.technion.vedibarta.utilities.resourcesManagement.RemoteFileResourcesManager
import com.technion.vedibarta.utilities.resourcesManagement.RemoteTextResourcesManager
import com.technion.vedibarta.utilities.resourcesManagement.getAllInDirectory
import java.io.File
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
            val textResourcesManager = RemoteTextResourcesManager(context)
            val fileResourcesManager = RemoteFileResourcesManager(context)
            getHobbiesResources(textResourcesManager, fileResourcesManager, _hobbiesResources)
            startedLoading = true
        }
    }

    private fun getHobbiesResources(
        textResourcesManager: RemoteTextResourcesManager,
        fileResourcesManager: RemoteFileResourcesManager,
        into: MutableLiveData<LoadableData<HobbiesResources>>
    ) {
        val task1 = textResourcesManager.findMultilingualResource("hobbies/all")
        val task2 = loadHobbies(context)
        val task3 = fileResourcesManager.getAllInDirectory("images/hobbies")

        task1.run {

            continueWithTask { allTask ->
                val allHobbies = allTask.result!!

                task2.continueWithTask { mapperTask ->
                    val hobbiesCardList = mapperTask.result!!

                    task3.continueWith { imageMap ->
                        HobbiesResources(allHobbies, hobbiesCardList, imageMap.result!!)
                    }
                }
            }
                .handleSuccess(into)
                .handleError(into)
                .handleTimeout(into)
        }
    }

    data class HobbiesResources(
        val allHobbies: MultilingualTextResource,
        val hobbyCardList: List<CategoryCard>,
        val hobbiesPhotos: Map<String, File>
    )

}
