package com.technion.vedibarta.data.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.technion.vedibarta.POJOs.Grade
import com.technion.vedibarta.R
import com.technion.vedibarta.data.TeacherResources

class TeacherSearchMatchViewModel : ViewModel() {

    private val _event = MutableLiveData<Event>()

    var school: String? = null
    var region: String? = null
    var grade: Grade? = null
    var searchBySchedule = false
    var characteristics = emptySet<String>()
    var subjects = emptySet<String>()

    val event: LiveData<Event> = _event

    fun searchPressed() {
        val schools = TeacherResources.schools
        val regions = TeacherResources.regions

        val region = region
        val school = school

        if (region != null && region !in regions.getAll()) {
            _event.value = Event.DisplayFailure(R.string.chat_search_wrong_region_message)
            return
        }

        if (school != null && school !in schools.getAll()) {
            _event.value = Event.DisplayFailure(R.string.chat_search_wrong_school_message)
            return
        }

        if (characteristics.isEmpty()) {
            _event.value = Event.DisplayFailure(R.string.teacher_search_no_characteristics_chosen_message)
            return
        }

        _event.value = Event.Search()
    }

    sealed class Event {
        var handled = false

        class Search : Event()
        data class DisplayFailure(val msgResId: Int) : Event()
    }
}