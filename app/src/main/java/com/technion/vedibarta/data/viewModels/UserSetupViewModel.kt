package com.technion.vedibarta.data.viewModels

import android.content.Context
import androidx.lifecycle.*
import com.google.android.gms.tasks.Task
import com.technion.vedibarta.POJOs.*
import com.technion.vedibarta.data.loadCharacteristics
import com.technion.vedibarta.utilities.extensions.executeAfterTimeoutInMillis
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

    private val characteristicsMale =
        MutableLiveData<LoadableData<MultilingualResource>>(NormalLoading())
    private val characteristicsFemale =
        MutableLiveData<LoadableData<MultilingualResource>>(NormalLoading())

    private val characteristicsWithCategoriesMale =
        MutableLiveData<LoadableData<Map<String, Array<String>>>>(NormalLoading())
    private val characteristicsWithCategoriesFemale =
        MutableLiveData<LoadableData<Map<String, Array<String>>>>(NormalLoading())

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
    var slowLoadingEventHandled: Boolean = false

    val chosenCharacteristics = mutableMapOf<String, Boolean>()

    val characteristicsByCategory: LiveData<LoadableData<Map<String, Array<String>>>> =
        Transformations.switchMap(gender) {
            when (it!!) {
                Gender.MALE -> characteristicsWithCategoriesMale
                Gender.FEMALE -> characteristicsWithCategoriesFemale
                Gender.NONE -> characteristicsWithCategoriesMale
            }
        }

    val characteristics: LiveData<LoadableData<MultilingualResource>> =
        Transformations.switchMap(gender) {
            when (it!!) {
                Gender.MALE -> characteristicsMale
                Gender.FEMALE -> characteristicsFemale
                Gender.NONE -> characteristicsMale
            }
        }

    val schoolsName: LiveData<LoadableData<Resource>> = _schoolsName
    val regionsName: LiveData<LoadableData<Resource>> = _regionsName
    val resourcesMediator = MediatorLiveData<LoadableData<Unit>>()

    init {
        loadResources()
        resourcesMediatorInit()
    }

    private fun loadResources() {
        val resourcesManager = RemoteResourcesManager(context)
        fun <T> Task<*>.handleError(data: MutableLiveData<LoadableData<T>>) =
            this.addOnFailureListener {
                data.value = Error(it.message)
            }

        fun <T> Task<out T>.handleSuccess(data: MutableLiveData<LoadableData<T>>) =
            this.addOnSuccessListener {
                data.value = Loaded<T>(it)
            }

        fun <T> Task<*>.handleTimeout(data: MutableLiveData<LoadableData<T>>) =
            this.executeAfterTimeoutInMillis {
                data.postValue(SlowLoadingEvent())
            }

        resourcesManager.findResource("schools")
            .handleSuccess(_schoolsName)
            .handleError(_schoolsName)
            .handleTimeout(_schoolsName)

        resourcesManager.findResource("regions")
            .handleSuccess(_regionsName)
            .handleError(_regionsName)
            .handleTimeout(_regionsName)


        resourcesManager.findMultilingualResource("characteristics/all", Gender.MALE)
            .handleSuccess(characteristicsMale)
            .handleError(characteristicsMale)
            .handleTimeout(characteristicsMale)

        resourcesManager.findMultilingualResource("characteristics/all", Gender.FEMALE)
            .handleSuccess(characteristicsFemale)
            .handleError(characteristicsFemale)
            .handleTimeout(characteristicsFemale)

        loadCharacteristics(context, Gender.MALE)
            .handleSuccess(characteristicsWithCategoriesMale)
            .handleError(characteristicsWithCategoriesMale)
            .handleTimeout(characteristicsWithCategoriesMale)

        loadCharacteristics(context, Gender.FEMALE)
            .handleSuccess(characteristicsWithCategoriesFemale)
            .handleError(characteristicsWithCategoriesFemale)
            .handleTimeout(characteristicsWithCategoriesFemale)
    }

    private fun resourcesMediatorInit() {

        resourcesMediator.addSource(schoolsName) {
            resourcesMediator.value = areResourcesLoaded()
        }
        resourcesMediator.addSource(regionsName) {
            resourcesMediator.value = areResourcesLoaded()
        }
        resourcesMediator.addSource(characteristics) {
            resourcesMediator.value = areResourcesLoaded()
        }
        resourcesMediator.addSource(characteristicsByCategory) {
            resourcesMediator.value = areResourcesLoaded()
        }

    }

    private fun areResourcesLoaded(): LoadableData<Unit> {

        fun <T> handleResourceState(data: LiveData<LoadableData<T>>) : LoadableData<Unit> =
            when (val it = data.value) {
                is Error -> Error(it.reason)
                is SlowLoadingEvent -> SlowLoadingEvent()
                is NormalLoading -> NormalLoading()
                else -> Loaded(Unit)
            }

        when (val it = handleResourceState(schoolsName)) {
            is Loaded -> {
            }
            else -> return it
        }

        when (val it = handleResourceState(regionsName)) {
            is Loaded -> {
            }
            else -> return it
        }

        when (val it = handleResourceState(characteristics)) {
            is Loaded -> {
            }
            else -> return it
        }

        when (val it = handleResourceState(characteristicsByCategory)) {
            is Loaded -> {
            }
            else -> return it
        }

        return Loaded(Unit)
    }

}




