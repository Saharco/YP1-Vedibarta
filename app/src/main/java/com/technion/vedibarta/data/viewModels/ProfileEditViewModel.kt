package com.technion.vedibarta.data.viewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Tasks
import com.technion.vedibarta.POJOs.*
import com.technion.vedibarta.R
import com.technion.vedibarta.data.loadCharacteristicsCardsWithTranslator
import com.technion.vedibarta.data.loadHobbiesCardsWithTranslator
import com.technion.vedibarta.utilities.VedibartaActivity
import com.technion.vedibarta.utilities.VedibartaActivity.Companion.student
import com.technion.vedibarta.utilities.extensions.handleError
import com.technion.vedibarta.utilities.extensions.handleSuccess
import com.technion.vedibarta.utilities.extensions.handleTimeout
import com.technion.vedibarta.utilities.resourcesManagement.MultilingualTextResource

class ProfileEditViewModel(context: Application): AndroidViewModel(context) {

    private val _resources = MutableLiveData<LoadableData<ProfileEditResources>>(NormalLoading())
    private val _event = MutableLiveData<Event>()

    private val initialCharacteristics = student?.characteristics?.keys ?: emptySet()
    private val initialHobbies = student?.hobbies?.toSet() ?: emptySet()

    private fun changesOccurred() =
        selectedCharacteristics != initialCharacteristics || selectedHobbies != initialHobbies

    private fun loadResources() {
        val gender = student?.gender ?: Gender.NONE

        val characteristicsTask = loadCharacteristicsCardsWithTranslator(getApplication(), gender)
        val hobbiesTask = loadHobbiesCardsWithTranslator(getApplication())

        Tasks.whenAll(characteristicsTask, hobbiesTask).continueWith {
            val characteristicsResult = characteristicsTask.result!!
            val hobbiesResult = hobbiesTask.result!!

            ProfileEditResources(
                characteristicsResult.translator,
                characteristicsResult.cards,
                hobbiesResult.translator,
                hobbiesResult.cards
            )
        }.apply {
            handleError(_resources)
            handleSuccess(_resources)
            handleTimeout(_resources)
        }
    }

    init {
        loadResources()
    }

    val resources: LiveData<LoadableData<ProfileEditResources>> = _resources
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

        VedibartaActivity.database.students().userId().build().set(student!!).addOnSuccessListener {
            Log.d("profileEdit", "saved profile changes")
            student?.characteristics = selectedCharacteristics.map { it to true }.toMap()
            student?.hobbies = selectedHobbies.toList()

            _event.value = Event.Finish.Success()
        }.addOnFailureListener {
            _event.value = Event.DisplayError(R.string.something_went_wrong)
        }
    }

    data class ProfileEditResources(
        val characteristicsTranslator: MultilingualTextResource,
        val characteristicsCardList: List<CategoryCard>,
        val hobbiesTranslator: MultilingualTextResource,
        val hobbyCardList: List<CategoryCard>
    )

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