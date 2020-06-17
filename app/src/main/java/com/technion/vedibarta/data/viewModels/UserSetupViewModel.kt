package com.technion.vedibarta.data.viewModels

import android.app.Application
import android.view.View
import androidx.lifecycle.*
import com.technion.vedibarta.POJOs.*
import com.technion.vedibarta.R
import com.technion.vedibarta.data.StudentResources
import com.technion.vedibarta.utilities.VedibartaActivity

class UserSetupViewModel(context: Application) : AndroidViewModel(context) {

    private val _doneButtonVisibility = MutableLiveData(View.GONE)
    private val _currentScreenIdx = MutableLiveData(0)
    private val _event = MutableLiveData<Event>()

    var grade: Grade = Grade.NONE
    var chosenFirstName: TextContainer = Unfilled
    var chosenLastName: TextContainer = Unfilled
    var chosenSchool: TextContainer = Unfilled
    var chosenRegion: TextContainer = Unfilled

    val characteristicsCardList = StudentResources.characteristicsCardList
    var selectedCharacteristics = List(characteristicsCardList.size) {
        MutableLiveData(emptySet<String>())
    }

    var selectedHobbies = emptySet<String>()

    val gender: LiveData<Gender> = StudentResources.gender
    val currentScreenIdx: LiveData<Int> = _currentScreenIdx
    val event: LiveData<Event> = _event

    fun setGender(gender: Gender) {
        StudentResources.gender.value = gender
    }

    var doneButtonVisibility: LiveData<Int> = _doneButtonVisibility

    var backButtonVisibility = Transformations.map(_currentScreenIdx) { idx ->
        if (idx == 0)
            View.GONE
        else
            View.VISIBLE
    }

    val nextButtonState = Transformations.switchMap<Int, NextButtonState>(_currentScreenIdx) { idx ->
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

    fun donePressed(userId: String) {
        val schools = StudentResources.schools
        val regions = StudentResources.regions

        val characteristics = selectedCharacteristics.map { it.value!! }.flatten()

        if (StudentResources.gender.value == Gender.NONE) {
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

        if (school !in schools.getAll()) {
            _event.value = Event.DisplayMissingInfoDialog(R.string.user_setup_school_missing)
            return
        }

        if (region !in regions.getAll()) {
            _event.value = Event.DisplayMissingInfoDialog(R.string.user_setup_region_missing)
            return
        }

        if (!validateSchoolAndRegionExists(school, region)) {
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
            gender = StudentResources.gender.value!!,
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

    private fun validateSchoolAndRegionExists(school: String, region: String): Boolean {
        val schools = StudentResources.schools
        val regions = StudentResources.regions
        val schoolAndRegionMap = schools.getAll().zip(regions.getAll()).toMap()

        return schoolAndRegionMap.containsKey(school) && schoolAndRegionMap[school] == region
    }

    fun nextPressed() {
        if (_currentScreenIdx.value == 0 && StudentResources.gender.value == Gender.NONE) {
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
