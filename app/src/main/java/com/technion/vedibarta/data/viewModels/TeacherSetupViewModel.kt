package com.technion.vedibarta.data.viewModels

import android.content.Context
import androidx.lifecycle.*
import com.google.android.material.card.MaterialCardView
import com.technion.vedibarta.POJOs.*
import com.technion.vedibarta.utilities.extensions.handleError
import com.technion.vedibarta.utilities.extensions.handleSuccess
import com.technion.vedibarta.utilities.extensions.handleTimeout
import com.technion.vedibarta.utilities.resourcesManagement.RemoteTextResourcesManager
import com.technion.vedibarta.utilities.resourcesManagement.TextResource
import java.lang.Class

fun teacherSetupViewModelFactory(context: Context) = object : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(TeacherSetupViewModel::class.java)) TeacherSetupViewModel(
            context
        ) as T else throw IllegalArgumentException()
    }
}

class TeacherSetupViewModel(private val context: Context) : ViewModel() {

    private val _event = MutableLiveData<Event>()
    private val _schoolsName = MutableLiveData<LoadableData<TextResource>>(NormalLoading())
    private val _regionsName = MutableLiveData<LoadableData<TextResource>>(NormalLoading())


    val gender = MutableLiveData(Gender.NONE)
    var chosenFirstName: TextContainer = Unfilled
    var chosenLastName: TextContainer = Unfilled

    var itemActionBarEnabled = false
    var selectedItems = 0
    val selectedItemsList = mutableListOf<MaterialCardView>()

    val schoolsList = mutableListOf<SchoolInfo>()

    private val schoolsName: LiveData<LoadableData<TextResource>> = _schoolsName
    private val regionsName: LiveData<LoadableData<TextResource>> = _regionsName

    val teacherSetupResources = combineResources(
        schoolsName, regionsName
    )

    val event: LiveData<Event> = _event

    init {
        loadResources()
    }

    private fun clearSelectedSchools() {
        itemActionBarEnabled = false
        selectedItems = 0
        selectedItemsList.forEach {
            it.isLongClickable = true
            it.isChecked = false
        }
        selectedItemsList.clear()
        _event.value = Event.ToggleActionBar()
    }

    fun unSelectSchool(v: MaterialCardView) {
        selectedItems--
        selectedItemsList.remove(v)
        v.isLongClickable = true
        v.isChecked = false
        if (selectedItems == 0) {
            itemActionBarEnabled = false
            _event.value = Event.ToggleActionBar()
        } else {
            _event.value = Event.UpdateTitle()
        }
    }


    fun beginSchoolExtraActions(v: MaterialCardView) {
        itemActionBarEnabled = true
        _event.value = Event.ToggleActionBar()
        selectSchool(v)
    }

    fun selectSchool(v: MaterialCardView) {
        selectedItems++
        selectedItemsList.add(v)
        v.isLongClickable = false
        v.isChecked = true
        _event.value = Event.UpdateTitle()
    }

    fun removeSelectedSchool() {
        selectedItemsList.forEach {
            val removeIndex = it.tag.toString().toInt()
            _event.value = Event.SchoolRemoved(removeIndex + 1)
            schoolsList.removeAt(removeIndex)
        }
        clearSelectedSchools()
    }

    fun addSchool(schoolInfo: SchoolInfo) {
        schoolsList.add(schoolInfo)
        _event.value = Event.SchoolAdded()
    }

    fun openSchoolDialog(isEdit: Boolean) {
        _event.value = Event.OpenSchoolDialog(isEdit)
    }

    fun editSchool(schoolInfo: SchoolInfo, editIndex: Int) {
        schoolsList[editIndex] = schoolInfo
        _event.value = Event.SchoolEdited(editIndex+1)
        unSelectSchool(selectedItemsList.first())
    }

    fun handleOnBackPress(): Boolean {
        if (itemActionBarEnabled) {
            clearSelectedSchools()
            return true
        }
        return false
    }

    private fun loadResources() {
        val resourcesManager = RemoteTextResourcesManager(context)

        resourcesManager.findResource("schools")
            .handleSuccess(_schoolsName)
            .handleError(_schoolsName)
            .handleTimeout(_schoolsName)

        resourcesManager.findResource("regions")
            .handleSuccess(_regionsName)
            .handleError(_regionsName)
            .handleTimeout(_regionsName)
    }

    sealed class Event {
        var handled = false
        class ToggleActionBar : Event()
        class UpdateTitle : Event()
        class SchoolAdded : Event()
        class OpenSchoolDialog(val isEdit: Boolean) : Event()
        data class SchoolEdited(val index: Int): Event()
        data class SchoolRemoved(val index: Int): Event()
        data class DisplayError(val msgResId: Int): Event()
    }
}

private fun combineResources(
    schoolsNameLiveData: LiveData<LoadableData<TextResource>>,
    regionsNameLiveData: LiveData<LoadableData<TextResource>>
): LiveData<LoadableData<TeacherSetupResources>> {
    val mediator = MediatorLiveData<LoadableData<TeacherSetupResources>>()
        .apply { value = NormalLoading() }

    fun refreshCombination() {
        // cannot go back after reaching an end-state
        if (mediator.value is Error) return

        val schoolsName = schoolsNameLiveData.value
        val regionsName = regionsNameLiveData.value

        // become Loaded when all resources have been loaded
        if (schoolsName is Loaded
            && regionsName is Loaded
        ) {
            mediator.value = Loaded(
                TeacherSetupResources(
                    schoolsName = schoolsName.data,
                    regionsName = regionsName.data
                )
            )
            return
        }

        // become Error when the first error occurs
        schoolsName?.let {
            if (it is Error) {
                mediator.value = Error(it.reason); return
            }
        }
        regionsName?.let {
            if (it is Error) {
                mediator.value = Error(it.reason); return
            }
        }

        // trigger SlowLoadingEvent when the first SlowLoadingEvent occurs
        if (mediator.value !is SlowLoadingEvent
            && (schoolsName is SlowLoadingEvent
                    || regionsName is SlowLoadingEvent)
        ) {
            mediator.value = SlowLoadingEvent()
            return
        }
    }

    mediator.addSource(schoolsNameLiveData) { refreshCombination() }
    mediator.addSource(regionsNameLiveData) { refreshCombination() }
    return mediator
}


data class TeacherSetupResources(
    val schoolsName: TextResource,
    val regionsName: TextResource
)

data class SchoolInfo(
    val schoolName: String,
    val schoolRegion: String,
    val grades: List<Grade>
){
    override fun equals(other: Any?): Boolean {
        return (other is SchoolInfo) && (schoolName == other.schoolName) && (schoolRegion == other.schoolRegion)
    }

    override fun hashCode(): Int {
        var result = schoolName.hashCode()
        result = 31 * result + schoolRegion.hashCode()
        result = 31 * result + grades.hashCode()
        return result
    }
}