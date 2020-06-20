package com.technion.vedibarta.data.viewModels

import android.text.SpannableStringBuilder
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.technion.vedibarta.POJOs.*
import com.technion.vedibarta.R
import java.lang.Class

fun schoolViewModelFactory(schoolsName: Array<String>, regionsName: Array<String>) = object : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(SchoolViewModel::class.java)) SchoolViewModel(
            schoolsName, regionsName
        ) as T else throw IllegalArgumentException()
    }
}

class SchoolViewModel(private val schoolsName: Array<String>, private val regionsName: Array<String>): ViewModel() {

    private val _event = MutableLiveData<SchoolEvent>()
    private val schoolAndRegionMap: Map<String, String> = schoolsName.zip(regionsName).toMap()

    var chosenSchoolNamePerSchool: TextContainer = Unfilled
    var chosenSchoolRegionPerSchool: TextContainer = Unfilled
    val chosenGradesPerSchool = mutableListOf<Grade>()

    val event: LiveData<SchoolEvent> = _event

    private fun validateSchoolInfo(): SchoolEvent{
        val school = when (val chosenSchool = chosenSchoolNamePerSchool) {
            is Unfilled -> {
                return SchoolEvent.DisplayMissingInfoDialog(R.string.user_setup_school_missing)
            }
            is Filled -> chosenSchool.text
        }

        if (!schoolsName.contains(school)){
            return SchoolEvent.DisplayMissingInfoDialog(R.string.user_setup_school_missing)

        }

        val region = when (val chosenRegion = chosenSchoolRegionPerSchool) {
            is Unfilled -> {
                return SchoolEvent.DisplayMissingInfoDialog(R.string.user_setup_region_missing)
            }
            is Filled -> chosenRegion.text
        }

        if (!regionsName.contains(region)){
            return SchoolEvent.DisplayMissingInfoDialog(R.string.user_setup_region_missing)
        }

        if (!(schoolAndRegionMap.containsKey(school) && schoolAndRegionMap[school] == region)){
            return SchoolEvent.DisplayMissingInfoDialog(R.string.user_setup_wrong_school_and_region_combination)
        }

        if (chosenGradesPerSchool.isEmpty()){
            return SchoolEvent.DisplayMissingInfoDialog(R.string.teacher_setup_grade_missing)
        }
        return SchoolEvent.SchoolAdded(SchoolInfo(school, region, chosenGradesPerSchool.toList()))
    }

    fun onSchoolSelectedListener(
        md: MaterialDialog,
        v: View
    ) {
        val regionSpinner = md.findViewById<AutoCompleteTextView>(R.id.regionListSpinner)
        val schoolName = (v as TextView).text.toString()

        val region = schoolAndRegionMap[schoolName].toString()
        chosenSchoolNamePerSchool = Filled(schoolName)
        chosenSchoolRegionPerSchool = Filled(region)
        regionSpinner.text = SpannableStringBuilder(region)
    }

    fun onRegionSelectedListener(
        md: MaterialDialog,
        v: View
    ) {
        val schoolSpinner = md.findViewById<AutoCompleteTextView>(R.id.schoolListSpinner)
        schoolSpinner.text = SpannableStringBuilder("")
        val region = (v as TextView).text.toString()
        val schoolList = schoolAndRegionMap.filter { it.value == region }.keys.toTypedArray()
        chosenSchoolNamePerSchool = Unfilled
        chosenSchoolRegionPerSchool = Filled(region)
        schoolSpinner.setAdapter(
            ArrayAdapter(
                md.context.applicationContext,
                android.R.layout.simple_dropdown_item_1line,
                schoolList
            )
        )
    }

    fun createSchool(
        schoolsList: List<SchoolInfo>
    ) {
        val event = when(val event = validateSchoolInfo()){
            is SchoolEvent.SchoolAdded -> event
            else -> {_event.value = event; return}
        }
        if (schoolsList.contains(event.school)){
            _event.value = SchoolEvent.DisplayMissingInfoDialog(R.string.teacher_setup_school_already_exist)
            return
        }

       _event.value = SchoolEvent.SchoolAdded(event.school)
        chosenGradesPerSchool.clear()
        chosenSchoolRegionPerSchool = Unfilled
        chosenSchoolNamePerSchool = Unfilled
    }

    fun editSchool(schoolsList: List<SchoolInfo>, editIndex: Int){
        val event = when(val event = validateSchoolInfo()){
            is SchoolEvent.SchoolAdded -> event
            else -> {_event.value = event; return}
        }
        if (schoolsList[editIndex] != event.school && schoolsList.contains(event.school)){
            _event.value = SchoolEvent.DisplayMissingInfoDialog(R.string.teacher_setup_school_already_exist)
            return
        }
        _event.value = SchoolEvent.SchoolEdited(event.school)
        chosenGradesPerSchool.clear()
        chosenSchoolRegionPerSchool = Unfilled
        chosenSchoolNamePerSchool = Unfilled
    }

    sealed class SchoolEvent {
        var handled = false

        data class SchoolAdded(val school: SchoolInfo) : SchoolEvent()
        data class SchoolEdited(val school: SchoolInfo) : SchoolEvent()
        data class DisplayMissingInfoDialog(val msgResId: Int) : SchoolEvent()
        data class DisplayError(val msgResId: Int) : SchoolEvent()
    }
}

