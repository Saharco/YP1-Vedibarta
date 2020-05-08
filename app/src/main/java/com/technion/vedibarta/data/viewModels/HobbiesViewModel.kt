package com.technion.vedibarta.data.viewModels

import android.content.Context
import androidx.lifecycle.*
import com.technion.vedibarta.POJOs.*
import com.technion.vedibarta.data.loadHobbies
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

    private val _hobbyCardList = MutableLiveData<LoadableData<List<HobbyCard>>>(NormalLoading())
    private val _hobbiesResource = MutableLiveData<LoadableData<MultilingualResource>>(NormalLoading())

    val hobbyCardList: LiveData<LoadableData<List<HobbyCard>>> = _hobbyCardList
    val hobbiesResource: LiveData<LoadableData<MultilingualResource>> = _hobbiesResource

    val chosenHobbies = mutableListOf<String>()

    val resourcesMediator = MediatorLiveData<Boolean>()

    private var startedLoading = false

    fun startLoading() {
        if (!startedLoading) {
            val resourcesManager = RemoteResourcesManager(context)

            loadHobbies(context).addOnSuccessListener {
                _hobbyCardList.value = Loaded(it)
            }

            resourcesManager.findMultilingualResource("hobbies/all").addOnSuccessListener {
                _hobbiesResource.value = Loaded(it)
            }

            resourcesMediatorInit()
            startedLoading = true
        }
    }

    private fun resourcesMediatorInit() {
        resourcesMediator.addSource(hobbyCardList) {
            resourcesMediator.value = areResourcesLoaded()
        }
        resourcesMediator.addSource(hobbiesResource) {
            resourcesMediator.value = areResourcesLoaded()
        }
    }

    private fun areResourcesLoaded(): Boolean {
        when (hobbyCardList.value) {
            is Loading -> return false
        }
        when (hobbiesResource.value) {
            is Loading -> return false
        }
        return true
    }

}
