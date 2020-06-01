package com.technion.vedibarta.data.viewModels

import android.app.Application
import android.view.View
import androidx.lifecycle.*
import com.google.android.gms.tasks.Tasks
import com.technion.vedibarta.POJOs.*
import com.technion.vedibarta.R
import com.technion.vedibarta.data.loadCharacteristicsCards
import com.technion.vedibarta.data.loadCharacteristicsTranslator
import com.technion.vedibarta.data.loadHobbiesCardsWithTranslator
import com.technion.vedibarta.utilities.VedibartaActivity
import com.technion.vedibarta.utilities.extensions.handleError
import com.technion.vedibarta.utilities.extensions.handleSuccess
import com.technion.vedibarta.utilities.extensions.handleTimeout
import com.technion.vedibarta.utilities.resourcesManagement.MultilingualTextResource
import com.technion.vedibarta.utilities.resourcesManagement.RemoteTextResourcesManager
import com.technion.vedibarta.utilities.resourcesManagement.TextResource

class UserSetupViewModel(context: Application) : AndroidViewModel(context) {

    data class UserSetupResources(
        val schoolNames: TextResource,
        val regionNames: TextResource,
        val characteristicsTranslator: LiveData<MultilingualTextResource>,
        val characteristicsCardList: List<CategoryCard>,
        val hobbiesTranslator: MultilingualTextResource,
        val hobbyCardList: List<CategoryCard>
    )

    private val _resources = MutableLiveData<LoadableData<UserSetupResources>>(NormalLoading())
    private val _gender = MutableLiveData(Gender.NONE)
    private val _doneButtonVisibility = MutableLiveData(View.GONE)
    private val _currentScreenIdx = MutableLiveData(0)
    private val _event = MutableLiveData<Event>()

    init {
        loadResources()
    }

    private fun loadResources() {
        val resourcesManager = RemoteTextResourcesManager(getApplication())

        val schoolsTask = resourcesManager.findResource("schools")
        val regionsTask = resourcesManager.findResource("regions")
        val characteristicsCardsTask = loadCharacteristicsCards(getApplication())
        val translatorMaleTask = loadCharacteristicsTranslator(getApplication(), Gender.MALE)
        val translatorFemaleTask = loadCharacteristicsTranslator(getApplication(), Gender.FEMALE)
        val hobbiesTask = loadHobbiesCardsWithTranslator(getApplication())

        val taskAll = Tasks.whenAll(
            schoolsTask,
            regionsTask,
            characteristicsCardsTask,
            translatorMaleTask,
            translatorFemaleTask,
            hobbiesTask
        )

        taskAll.continueWith {
            repeat(characteristicsCardsTask.result!!.size) {
                selectedCharacteristics.add(MutableLiveData(emptySet()))
            }

            UserSetupResources(
                schoolsTask.result!!,
                regionsTask.result!!,
                Transformations.map(_gender) {
                    when (it!!) {
                        Gender.MALE -> translatorMaleTask.result!!
                        Gender.FEMALE -> translatorFemaleTask.result!!
                        Gender.NONE -> translatorMaleTask.result!!
                    }
                },
                characteristicsCardsTask.result!!,
                hobbiesTask.result!!.translator,
                hobbiesTask.result!!.cards
            )
        }.apply {
            handleTimeout(_resources)
            handleSuccess(_resources)
            handleError(_resources)
        }
    }

    var grade: Grade = Grade.NONE
    var chosenFirstName: TextContainer = Unfilled

    var chosenLastName: TextContainer = Unfilled

    var chosenSchool: TextContainer = Unfilled
    var chosenRegion: TextContainer = Unfilled

    fun setGender(gender: Gender) {
        _gender.value = gender
    }

    var doneButtonVisibility: LiveData<Int> = _doneButtonVisibility

    var backButtonVisibility = Transformations.switchMap(_resources) {
        when (it) {
            is Loaded -> Transformations.map(_currentScreenIdx) { idx ->
                if (idx == 0)
                    View.GONE
                else
                    View.VISIBLE
            }
            else -> MutableLiveData(View.GONE)
        }
    }

    val nextButtonState = Transformations.switchMap<LoadableData<UserSetupResources>, NextButtonState>(_resources) { resources ->
        when (resources) {
            is Loaded -> Transformations.switchMap<Int, NextButtonState>(_currentScreenIdx) { idx ->
                when (idx) {
                    0 -> MutableLiveData(NextButtonState.Visible(R.string.next))
                    selectedCharacteristics.size + 1 -> MutableLiveData(NextButtonState.Gone)
                    else -> {
                        Transformations.map(selectedCharacteristics[idx - 1]) {
                            if (it == emptySet<String>())
                                NextButtonState.Visible(R.string.skip)
                            else
                                NextButtonState.Visible(R.string.next)
                        }
                    }
                }
            }
            else -> MutableLiveData(NextButtonState.Gone)
        }
    }

    var selectedCharacteristics = mutableListOf<MutableLiveData<Set<String>>>()
    var selectedHobbies = emptySet<String>()

    val resources: LiveData<LoadableData<UserSetupResources>> = _resources
    val gender: LiveData<Gender> = _gender
    val currentScreenIdx: LiveData<Int> = _currentScreenIdx
    val event: LiveData<Event> = _event

    fun donePressed(userId: String) {
        val resources = (resources.value as? Loaded)?.data
            ?: error("pressed done before done loading resources")

        val characteristics = selectedCharacteristics.map { it.value!! }.flatten()

        if (_gender.value == Gender.NONE) {
            _event.value = Event.DisplayMissingInfoDialog(R.string.user_setup_gender_missing)
            return
        }

        val firstName = when (val chosenFirstName = chosenFirstName) {
            is Filled -> chosenFirstName.text
            is Unfilled -> {
                _event.value = Event.DisplayMissingInfoDialog(R.string.user_setup_first_name_missing)
                return
            }
        }

        val lastName = when (val chosenLastName = chosenLastName) {
            is Filled -> chosenLastName.text
            is Unfilled -> {
                _event.value = Event.DisplayMissingInfoDialog(R.string.user_setup_last_name_missing)
                return
            }
        }

        if (grade == Grade.NONE) {
            _event.value = Event.DisplayMissingInfoDialog(R.string.user_setup_grade_missing)
            return
        }

        val school = when (val chosenSchool = chosenSchool) {
            is Filled -> chosenSchool.text
            is Unfilled -> {
                _event.value = Event.DisplayMissingInfoDialog(R.string.user_setup_school_missing)
                return
            }
        }

        val region = when (val chosenRegion = chosenRegion) {
            is Filled -> chosenRegion.text
            is Unfilled -> {
                _event.value = Event.DisplayMissingInfoDialog(R.string.user_setup_region_missing)
                return
            }
        }

        if (school !in resources.schoolNames.getAll()) {
            _event.value = Event.DisplayMissingInfoDialog(R.string.user_setup_school_missing)
            return
        }

        if (region !in resources.regionNames.getAll()) {
            _event.value = Event.DisplayMissingInfoDialog(R.string.user_setup_region_missing)
            return
        }

        if (!validateSchoolAndRegionExists(resources, school, region)) {
            _event.value = Event.DisplayMissingInfoDialog(R.string.user_setup_wrong_school_and_region_combination)
            return
        }

        if (characteristics.isEmpty()) {
            _event.value = Event.DisplayMissingInfoDialog(R.string.user_setup_characteristics_missing)
            return
        }

        if (selectedHobbies.isEmpty()) {
            _event.value = Event.DisplayMissingInfoDialog(R.string.user_setup_hobbies_missing)
            return
        }

        val student = Student(
            uid = userId,
            name = "$firstName $lastName",
            gender = _gender.value!!,
            region = region,
            school = school,
            grade = grade,
            characteristics = characteristics.map { it to true }.toMap().toMutableMap(),
            hobbies = selectedHobbies.toList()
        )

        VedibartaActivity.database.students().userId().build().set(student)
            .addOnSuccessListener {
                VedibartaActivity.student = student
                _event.value = Event.Finish()
            }
            .addOnFailureListener {
                _event.value = Event.DisplayError(R.string.something_went_wrong)
            }
    }

    private fun validateSchoolAndRegionExists(
        resources: UserSetupResources,
        school: String,
        region: String
    ): Boolean {
        val schoolNamesList = resources.schoolNames
        val regionNamesList = resources.regionNames
        val schoolAndRegionMap = schoolNamesList.getAll().zip(regionNamesList.getAll()).toMap()

        return schoolAndRegionMap.containsKey(school) && schoolAndRegionMap[school] == region
    }

    fun nextPressed() {
        if (_currentScreenIdx.value == 0 && _gender.value == Gender.NONE) {
            _event.value = Event.DisplayError(R.string.user_setup_gender_missing)
            return
        }

        _currentScreenIdx.value = _currentScreenIdx.value!! + 1
        if (_currentScreenIdx.value == selectedCharacteristics.size + 1)
            _doneButtonVisibility.value = View.VISIBLE
    }

    fun backPressed() {
        if (_currentScreenIdx.value == 0) return

        _currentScreenIdx.value = _currentScreenIdx.value!! - 1
    }

    sealed class NextButtonState {
        object Gone : NextButtonState()
        data class Visible(val textResId: Int) : NextButtonState()
    }

    sealed class Event {
        var handled = false

        class Finish : Event()
        data class DisplayMissingInfoDialog(val msgResId: Int) : Event()
        data class DisplayError(val msgResId: Int): Event()
    }
}
