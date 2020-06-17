package com.technion.vedibarta.data.viewModels

import android.app.Application
import androidx.lifecycle.*
import com.technion.vedibarta.POJOs.*
import com.technion.vedibarta.R
import com.technion.vedibarta.data.StudentResources

class ChatSearchViewModel(context: Application) : AndroidViewModel(context) {

    private val _event = MutableLiveData<Event>()

    var grade: Grade = Grade.NONE
    var chosenSchool: TextContainer = Unfilled
    var chosenRegion: TextContainer = Unfilled
    var selectedCharacteristics: Set<String> = emptySet()

    val event: LiveData<Event> = _event

    fun searchPressed() {
        val schools = StudentResources.schools
        val regions = StudentResources.regions

        when (val region = chosenRegion) {
            is Filled -> {
                if (region.text !in regions.getAll()) {
                    _event.value = Event.DisplayFailure(R.string.chat_search_wrong_region_message)
                    return
                }
            }
        }

        when(val school = chosenSchool) {
            is Filled -> {
                if (school.text !in schools.getAll()) {
                    _event.value = Event.DisplayFailure(R.string.chat_search_wrong_school_message)
                    return
                }
            }
        }

        if (selectedCharacteristics.isEmpty()) {
            _event.value = Event.DisplayFailure(R.string.chat_search_no_characteristics_chosen_message)
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
