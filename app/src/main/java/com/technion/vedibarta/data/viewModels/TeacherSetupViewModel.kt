package com.technion.vedibarta.data.viewModels

import android.app.Application
import android.view.View
import androidx.lifecycle.*
import com.technion.vedibarta.POJOs.*
import com.technion.vedibarta.R
import com.technion.vedibarta.data.StudentResources
import com.technion.vedibarta.data.TeacherMeta
import com.technion.vedibarta.data.TeacherResources
import com.technion.vedibarta.database.DatabaseVersioning
import com.technion.vedibarta.utilities.VedibartaActivity.Companion.userId

class TeacherSetupViewModel(application: Application) : AndroidViewModel(application) {

    private val _doneButtonVisibility = MutableLiveData(View.GONE)
    private val _currentScreenIdx = MutableLiveData(0)
    private val _event = MutableLiveData<Event>()

    private val schedule = mutableTimetableOf()

    val gender = MutableLiveData(Gender.NONE)
    var firstName: String? = null
    var lastName: String? = null
    val schoolsInfo = mutableListOf<SchoolInfo>()

    var selectedCharacteristics = emptySet<String>()
    var selectedSubjects = emptySet<String>()

    val currentScreenIdx: LiveData<Int> = _currentScreenIdx
    val event: LiveData<Event> = _event

    val doneButtonVisibility: LiveData<Int> = _doneButtonVisibility

    val nextButtonState = Transformations.switchMap<Int, NextButtonState>(_currentScreenIdx) { idx ->
        when (idx) {
            3 -> MutableLiveData(NextButtonState.Gone)
            else -> MutableLiveData(NextButtonState.Visible)
        }
    }

    fun scheduleTimeChanged(time: DayHour, isPressed: Boolean) {
        if (isPressed)
            schedule.add(time)
        else
            schedule.remove(time)
    }

    fun donePressed() {
        if (gender.value == Gender.NONE) {
            _event.value = Event.DisplayMissingInfoDialog(R.string.user_setup_gender_missing)
            return
        }

        if (firstName == null) {
            _event.value = Event.DisplayMissingInfoDialog(R.string.user_setup_first_name_missing)
            return
        }

        if (lastName == null) {
            _event.value = Event.DisplayMissingInfoDialog(R.string.user_setup_last_name_missing)
            return
        }

        if (schoolsInfo.isEmpty()) {
            _event.value = Event.DisplayMissingInfoDialog(R.string.teacher_setup_no_schools_entered)
            return
        }

        if (selectedCharacteristics.isEmpty()) {
            _event.value = Event.DisplayMissingInfoDialog(R.string.teacher_setup_characteristics_missing)
            return
        }

        if (selectedSubjects.isEmpty()) {
            _event.value = Event.DisplayMissingInfoDialog(R.string.teacher_setup_subjects_missing)
            return
        }

        if (schedule.isEmpty()) {
            _event.value = Event.DisplayMissingInfoDialog(R.string.teacher_setup_schedule_missing)
            return
        }

        val teacher = Teacher(
            "$firstName $lastName",
            null,
            gender.value!!,
            userId!!,
            schoolsInfo.map { it.schoolRegion },
            schoolsInfo.map { it.schoolName },
            selectedCharacteristics.map { it to true }.toMap().toMutableMap(),
            Teacher.Grades(*schoolsInfo.flatMap { it.grades }.toTypedArray()),
            selectedSubjects.map { it to true }.toMap().toMutableMap(),
            schedule
        )

        DatabaseVersioning.currentVersion.instance.collection("teachers").document(userId!!).set(teacher)
            .addOnSuccessListener {
                TeacherMeta.teacher = teacher
                _event.value = Event.Finish()
            }
            .addOnFailureListener {
                _event.value = Event.DisplayError(R.string.something_went_wrong)
            }
    }

    fun nextPressed() {
        _currentScreenIdx.value = _currentScreenIdx.value!! + 1
        if (_currentScreenIdx.value == 3)
            _doneButtonVisibility.value = View.VISIBLE
    }

    fun backPressed() {
        if (_currentScreenIdx.value == 0) {
            _event.value = Event.Back()
            return
        }

        _currentScreenIdx.value = _currentScreenIdx.value!! - 1
    }

    open inner class SchoolDialogViewModel(protected val position: Int) {
        var school: String? = null
        var region: String? = null
        val grades = mutableSetOf<Grade>()

        val allSchools = TeacherResources.schools.getAll()
        val allRegions = TeacherResources.regions.getAll()
        val schoolAndRegionMap = allSchools.zip(allRegions).toMap()

        fun save(): AddResult {
            val selectedSchool = school
            val selectedRegion = region

            if (selectedSchool == null) {
                _event.value = Event.DisplayMissingInfoDialog(R.string.user_setup_school_missing)
                return AddResult.Failure
            }

            if (selectedSchool !in allSchools) {
                _event.value = Event.DisplayMissingInfoDialog(R.string.user_setup_school_missing)
                return AddResult.Failure
            }

            if (selectedRegion == null) {
                _event.value = Event.DisplayMissingInfoDialog(R.string.user_setup_region_missing)
                return AddResult.Failure
            }

            if (selectedRegion !in allRegions) {
                _event.value = Event.DisplayMissingInfoDialog(R.string.user_setup_region_missing)
                return AddResult.Failure
            }

            if (!(selectedSchool in schoolAndRegionMap.keys && schoolAndRegionMap[selectedSchool] == selectedRegion)) {
                _event.value = Event.DisplayMissingInfoDialog(R.string.user_setup_wrong_school_and_region_combination)
                return AddResult.Failure
            }

            if (grades.isEmpty()) {
                _event.value = Event.DisplayMissingInfoDialog(R.string.teacher_setup_grade_missing)
                return AddResult.Failure
            }

            val schoolInfo = SchoolInfo(selectedSchool, selectedRegion, grades)

            val filteredSchools =  if (position < schoolsInfo.size)
                schoolsInfo.toMutableList().apply { removeAt(position) }
            else
                schoolsInfo

            if (filteredSchools.any { it.isSameSchoolAs(schoolInfo) }) {
                _event.value = Event.DisplayMissingInfoDialog(R.string.teacher_setup_school_already_exist)
                return AddResult.Failure
            }

            if (position < schoolsInfo.size)
                schoolsInfo[position] = schoolInfo
            else
                schoolsInfo += schoolInfo
            return AddResult.Success(position)
        }
    }

    inner class EditableSchoolDialogViewModel(position: Int) : SchoolDialogViewModel(position) {
        init {
            val schoolInfo = schoolsInfo[position]
            school = schoolInfo.schoolName
            region = schoolInfo.schoolRegion
            grades.addAll(schoolInfo.grades)
        }

        fun remove() {
            schoolsInfo.removeAt(position)
        }
    }

    fun getAddSchoolDialogViewModel() = SchoolDialogViewModel(schoolsInfo.size)
    fun getEditSchoolDialogViewModel(position: Int) = EditableSchoolDialogViewModel(position)

    sealed class AddResult {
        data class Success(val insertPos: Int) : AddResult()
        object Failure : AddResult()
    }

    sealed class NextButtonState {
        object Gone : NextButtonState()
        object Visible : NextButtonState()
    }

    sealed class Event {
        var handled = false

        class Finish : Event()
        class Back : Event()
        data class DisplayMissingInfoDialog(val msgResId: Int) : Event()
        data class DisplayError(val msgResId: Int): Event()
    }
}

data class SchoolInfo(
    val schoolName: String,
    val schoolRegion: String,
    val grades: Set<Grade>
) {
    fun isSameSchoolAs(other: SchoolInfo) =
        schoolName == other.schoolName && schoolRegion == other.schoolRegion
}