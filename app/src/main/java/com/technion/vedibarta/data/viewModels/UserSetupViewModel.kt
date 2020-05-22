package com.technion.vedibarta.data.viewModels

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.technion.vedibarta.POJOs.*
import com.technion.vedibarta.R
import com.technion.vedibarta.data.CategoriesMapper
import com.technion.vedibarta.data.loadCharacteristics
import com.technion.vedibarta.utilities.extensions.handleError
import com.technion.vedibarta.utilities.extensions.handleSuccess
import com.technion.vedibarta.utilities.extensions.handleTimeout
import com.technion.vedibarta.utilities.resourcesManagement.MultilingualResource
import com.technion.vedibarta.utilities.resourcesManagement.RemoteResourcesManager
import com.technion.vedibarta.utilities.resourcesManagement.Resource
import java.lang.Class

fun userSetupViewModelFactory(context: Context) = object : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(UserSetupViewModel::class.java)) UserSetupViewModel(
            context
        ) as T else throw IllegalArgumentException()
    }
}

class UserSetupViewModel(private val context: Context) : ViewModel() {

    private val _characteristicsResourcesMale =
        MutableLiveData<LoadableData<CharacteristicsResources>>(NormalLoading())

    private val _characteristicsResourcesFemale =
        MutableLiveData<LoadableData<CharacteristicsResources>>(NormalLoading())

    private val _schoolsName = MutableLiveData<LoadableData<Resource>>(NormalLoading())
    private val _regionsName = MutableLiveData<LoadableData<Resource>>(NormalLoading())

    val gender = MutableLiveData(Gender.NONE)

    var grade: Grade = Grade.NONE
    var chosenFirstName: TextContainer = Unfilled
    var chosenLastName: TextContainer = Unfilled
    var chosenSchool: TextContainer = Unfilled
    var chosenRegion: TextContainer = Unfilled

    var reachedLastPage: Boolean = false
    var backButtonVisible: Boolean = false
    var nextButtonText: String = context.resources.getString(R.string.next)


    val chosenCharacteristics = mutableMapOf<String, Boolean>()

    private val characteristicsResources: LiveData<LoadableData<CharacteristicsResources>> =
        Transformations.switchMap(gender) {
            when (it!!) {
                Gender.MALE -> _characteristicsResourcesMale
                Gender.FEMALE -> _characteristicsResourcesFemale
                Gender.NONE -> _characteristicsResourcesMale
            }
        }

    private val schoolsName: LiveData<LoadableData<Resource>> = _schoolsName
    private val regionsName: LiveData<LoadableData<Resource>> = _regionsName

    val userSetupResources = combineResources(
        schoolsName,
        regionsName,
        characteristicsResources
    )

    init {
        loadResources()
    }

    private fun loadResources() {
        val resourcesManager = RemoteResourcesManager(context)


        fun getCharacteristicsResources(gender: Gender, into: MutableLiveData<LoadableData<CharacteristicsResources>>) {
            val task1 = resourcesManager.findMultilingualResource("characteristics/all", gender)
            val task2 = loadCharacteristics(context, gender)

            task1.continueWithTask { allTask ->
                val allCharacteristics = allTask.result!!

                task2.continueWith { mapperTask ->
                    val characteristicsMapper = mapperTask.result!!

                    CharacteristicsResources(allCharacteristics, characteristicsMapper)
                }
            }
                .handleSuccess(into)
                .handleError(into)
                .handleTimeout(into)
        }

        resourcesManager.findResource("schools")
            .handleSuccess(_schoolsName)
            .handleError(_schoolsName)
            .handleTimeout(_schoolsName)

        resourcesManager.findResource("regions")
            .handleSuccess(_regionsName)
            .handleError(_regionsName)
            .handleTimeout(_regionsName)

        getCharacteristicsResources(Gender.MALE, _characteristicsResourcesMale)
        getCharacteristicsResources(Gender.FEMALE, _characteristicsResourcesFemale)
    }
}

private fun combineResources(
    schoolsNameLiveData: LiveData<LoadableData<Resource>>,
    regionsNameLiveData: LiveData<LoadableData<Resource>>,
    characteristicsResourcesLiveData: LiveData<LoadableData<CharacteristicsResources>>
): LiveData<LoadableData<UserSetupResources>> {
    val mediator = MediatorLiveData<LoadableData<UserSetupResources>>()
        .apply { value = NormalLoading() }

    fun refreshCombination() {
        // cannot go back after reaching an end-state
        if (mediator.value is Error) return

        val schoolsName = schoolsNameLiveData.value
        val regionsName = regionsNameLiveData.value
        val characteristicsResources = characteristicsResourcesLiveData.value

        // become Loaded when all resources have been loaded
        if (schoolsName is Loaded
            && regionsName is Loaded
            && characteristicsResources is Loaded
        ) {
            mediator.value = Loaded(UserSetupResources(
                schoolsName = schoolsName.data,
                regionsName = regionsName.data,
                allCharacteristics = characteristicsResources.data.allCharacteristics,
                characteristicsByCategory = characteristicsResources.data.characteristicsByCategory
            ))
            return
        }

        // become Error when the first error occurs
        schoolsName?.let { if (it is Error) {mediator.value = Error(it.reason); return }}
        regionsName?.let { if (it is Error) {mediator.value = Error(it.reason); return }}
        characteristicsResources?.let { if (it is Error) {mediator.value = Error(it.reason); return }}

        // trigger SlowLoadingEvent when the first SlowLoadingEvent occurs
        if (mediator.value !is SlowLoadingEvent
            && (schoolsName is SlowLoadingEvent
            || regionsName is SlowLoadingEvent
            || characteristicsResources is SlowLoadingEvent)
        ) {
            mediator.value = SlowLoadingEvent()
            return
        }
    }

    mediator.addSource(schoolsNameLiveData) { refreshCombination() }
    mediator.addSource(regionsNameLiveData) { refreshCombination() }
    mediator.addSource(characteristicsResourcesLiveData) { refreshCombination() }

    return mediator
}

data class CharacteristicsResources(
    val allCharacteristics: MultilingualResource,
    val characteristicsByCategory: CategoriesMapper
)

data class UserSetupResources(
    val schoolsName: Resource,
    val regionsName: Resource,
    val allCharacteristics: MultilingualResource,
    val characteristicsByCategory: CategoriesMapper
)
