package com.technion.vedibarta.data.viewModels

import android.content.Context
import androidx.lifecycle.*
import com.technion.vedibarta.POJOs.*
import com.technion.vedibarta.data.loadCharacteristics
import com.technion.vedibarta.data.loadHobbies
import com.technion.vedibarta.utilities.resourcesManagement.MultilingualResource
import com.technion.vedibarta.utilities.resourcesManagement.RemoteResourcesManager
import com.technion.vedibarta.utilities.resourcesManagement.Resource
import java.lang.Class
import java.lang.IllegalArgumentException

fun userSetupViewModelFactory(context: Context) = object : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(UserSetupViewModel::class.java)) UserSetupViewModel(
            context
        ) as T else throw IllegalArgumentException()
    }
}

class UserSetupViewModel(private val context: Context) : ViewModel() {

    private val _hobbyCardList = MutableLiveData<LoadableData<List<HobbyCard>>>(Loading())
    private val _hobbiesResource = MutableLiveData<LoadableData<MultilingualResource>>(Loading())

    private val characteristicsMale = MutableLiveData<LoadableData<MultilingualResource>>(Loading())
    private val characteristicsFemale = MutableLiveData<LoadableData<MultilingualResource>>(Loading())

    private val characteristicsWithCategoriesMale = MutableLiveData<LoadableData<Map<String, Array<String>>>>(Loading())
    private val characteristicsWithCategoriesFemale = MutableLiveData<LoadableData<Map<String, Array<String>>>>(Loading())

    private val _schoolsName = MutableLiveData<LoadableData<Resource>>(Loading())
    private val _regionsName = MutableLiveData<LoadableData<Resource>>(Loading())

    val gender= MutableLiveData(Gender.NONE)

    var grade: Grade = Grade.NONE
    var chosenFirstName: TextContainer = Unfilled
    var chosenLastName: TextContainer = Unfilled
    var chosenSchool: TextContainer = Unfilled
    var chosenRegion: TextContainer = Unfilled

    val chosenHobbies = mutableListOf<String>()
    val chosenCharacteristics = mutableMapOf<String, Boolean>()

    val characteristicsByCategory: LiveData<LoadableData<Map<String, Array<String>>>> = Transformations.switchMap(gender){
        when(it!!){
            Gender.MALE ->characteristicsWithCategoriesMale
            Gender.FEMALE -> characteristicsWithCategoriesFemale
            Gender.NONE -> characteristicsWithCategoriesMale
        }
    }

    val characteristics: LiveData<LoadableData<MultilingualResource>> = Transformations.switchMap(gender){
        when(it!!){
            Gender.MALE ->characteristicsMale
            Gender.FEMALE -> characteristicsFemale
            Gender.NONE -> characteristicsMale
        }
    }

    val hobbyCardList: LiveData<LoadableData<List<HobbyCard>>> = _hobbyCardList
    val hobbiesResource: LiveData<LoadableData<MultilingualResource>> = _hobbiesResource

    val schoolsName: LiveData<LoadableData<Resource>> = _schoolsName
    val regionsName: LiveData<LoadableData<Resource>> = _regionsName
    val resourcesMediator = MediatorLiveData<Boolean>()
    init {
        loadResources()
        resourcesMediatorInit()
    }

    private fun loadResources(){
        val resourcesManager = RemoteResourcesManager(context)

        loadHobbies(context).addOnSuccessListener {
            _hobbyCardList.value = Loaded(it)
        }

        resourcesManager.findMultilingualResource("hobbies/all").addOnSuccessListener {
            _hobbiesResource.value = Loaded(it)
        }

        resourcesManager.findResource("schools").addOnSuccessListener {
            _schoolsName.value = Loaded(it)
        }

        resourcesManager.findResource("regions").addOnSuccessListener {
            _regionsName.value = Loaded(it)
        }

        resourcesManager.findMultilingualResource("characteristics/all", Gender.MALE)
            .addOnSuccessListener {
                characteristicsMale.value = Loaded(it)
            }

        resourcesManager.findMultilingualResource("characteristics/all", Gender.FEMALE)
            .addOnSuccessListener {
                characteristicsFemale.value = Loaded(it)
            }

        loadCharacteristics(context, Gender.MALE)
            .addOnSuccessListener {
                characteristicsWithCategoriesMale.value = Loaded(it)
            }

        loadCharacteristics(context, Gender.FEMALE)
            .addOnSuccessListener {
                characteristicsWithCategoriesFemale.value = Loaded(it)
            }
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

    private fun areResourcesLoaded(): Boolean {
        when (schoolsName.value) {
            is Loading -> return false
        }
        when (regionsName.value) {
            is Loading -> return false
        }
        when (characteristics.value) {
            is Loading -> return false
        }
        when (characteristicsByCategory.value) {
            is Loading -> return false
        }

        return true
    }

}

sealed class LoadableData<out T>

class Loading<T> : LoadableData<T>()

data class Loaded<T>(val data: T) : LoadableData<T>()
