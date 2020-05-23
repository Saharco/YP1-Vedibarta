package com.technion.vedibarta.data.viewModels

import android.content.Context
import androidx.lifecycle.*
import com.technion.vedibarta.POJOs.*
import com.technion.vedibarta.data.CategoriesMapper
import com.technion.vedibarta.data.loadCharacteristics
import com.technion.vedibarta.utilities.extensions.handleError
import com.technion.vedibarta.utilities.extensions.handleSuccess
import com.technion.vedibarta.utilities.extensions.handleTimeout
import com.technion.vedibarta.utilities.resourcesManagement.MultilingualTextResource
import com.technion.vedibarta.utilities.resourcesManagement.RemoteTextResourcesManager
import java.lang.Class
import java.lang.IllegalArgumentException

fun characteristicsViewModelFactory(context: Context, gender: Gender) =
    object : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(CharacteristicsViewModel::class.java)) CharacteristicsViewModel(
                context, gender
            ) as T else throw IllegalArgumentException()
        }
    }

class CharacteristicsViewModel(private val context: Context, private val gender: Gender) :
    ViewModel() {

    private val _characteristicsResources =
        MutableLiveData<LoadableData<CharacteristicsResources>>(NormalLoading())

    val characteristicsResources: LiveData<LoadableData<CharacteristicsResources>> = _characteristicsResources

    val chosenCharacteristics = mutableListOf<String>()

    private var startedLoading = false

    fun startLoading() {
        if (!startedLoading) {
            val resourcesManager = RemoteTextResourcesManager(context)

            getCharacteristicsResources(resourcesManager, gender,_characteristicsResources)

            startedLoading = true
        }
    }

    private fun getCharacteristicsResources(
        resourcesManager: RemoteTextResourcesManager,
        gender: Gender,
        into: MutableLiveData<LoadableData<CharacteristicsResources>>
    ) {
        val task1 = resourcesManager.findMultilingualResource("characteristics/all", gender)
        val task2 = loadCharacteristics(context, gender)

        task1.run {

            continueWithTask { allTask ->
                val allCharacteristics = allTask.result!!

                task2.continueWith { mapperTask ->
                    val characteristicsMapper = mapperTask.result!!.toCategoryCardList()

                    CharacteristicsResources(allCharacteristics, characteristicsMapper)
                }
            }
                .handleSuccess(into)
                .handleError(into)
                .handleTimeout(into)
        }
    }

    private fun CategoriesMapper.toCategoryCardList(): List<CategoryCard> {
        return this.map { (key, value) ->
            CategoryCard(key, value.getAll().toTypedArray())
        }
    }

    data class CharacteristicsResources(
        val allCharacteristics: MultilingualTextResource,
        val characteristicsCardList: List<CategoryCard>
    )
}

