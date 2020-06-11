package com.technion.vedibarta.data.viewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.technion.vedibarta.R
import com.technion.vedibarta.utilities.VedibartaActivity
import com.technion.vedibarta.utilities.VedibartaActivity.Companion.student

class ProfileEditViewModel(context: Application): AndroidViewModel(context) {

    private val _event = MutableLiveData<Event>()

    private val initialCharacteristics = student?.characteristics?.keys ?: emptySet()
    private val initialHobbies = student?.hobbies?.toSet() ?: emptySet()

    private fun changesOccurred() =
        selectedCharacteristics != initialCharacteristics || selectedHobbies != initialHobbies

    val event: LiveData<Event> = _event

    var selectedCharacteristics: Set<String> = initialCharacteristics
    var selectedHobbies: Set<String> = initialHobbies

    fun backPressed() {
        _event.value = if (!changesOccurred()) {
            Event.Finish.Cancel()
        } else {
            Event.DisplayConfirmationDialog()
        }
    }

    fun confirmationCancelPressed() {
        _event.value = Event.Finish.Cancel()
    }

    fun commitChangesPressed() {
        if (!changesOccurred()) {
            _event.value = Event.Finish.Cancel()
            return
        }

        Log.d("profileEdit", "saved profile changes")
        student?.characteristics = selectedCharacteristics.map { it to true }.toMap()
        student?.hobbies = selectedHobbies.toList()

        VedibartaActivity.database.students().userId().build().set(student!!).addOnSuccessListener {
            _event.value = Event.Finish.Success()
        }.addOnFailureListener {
            student?.characteristics = initialCharacteristics.map { it to true }.toMap()
            student?.hobbies = initialHobbies.toList()

            _event.value = Event.DisplayError(R.string.something_went_wrong)
        }
    }

    sealed class Event {
        var handled = false

        class DisplayConfirmationDialog : Event()
        data class DisplayError(val errorMsgId: Int) : Event()

        sealed class Finish : Event() {
            class Cancel : Finish()
            class Success : Finish()
        }
    }
}