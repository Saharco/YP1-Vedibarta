package com.technion.vedibarta.data.viewModels

import android.app.Application
import androidx.lifecycle.*
import com.google.android.gms.tasks.Tasks
import com.technion.vedibarta.POJOs.*
import com.technion.vedibarta.R
import com.technion.vedibarta.data.loadCharacteristicsCardsWithTranslator
import com.technion.vedibarta.utilities.VedibartaActivity.Companion.student
import com.technion.vedibarta.utilities.extensions.handleError
import com.technion.vedibarta.utilities.extensions.handleSuccess
import com.technion.vedibarta.utilities.extensions.handleTimeout
import com.technion.vedibarta.utilities.resourcesManagement.MultilingualTextResource
import com.technion.vedibarta.utilities.resourcesManagement.RemoteTextResourcesManager
import com.technion.vedibarta.utilities.resourcesManagement.TextResource

class ChatSearchViewModel(context: Application) : AndroidViewModel(context) {

    data class ChatSearchResources(
        val characteristicsTranslator: MultilingualTextResource,
        val characteristicsCardList: List<CategoryCard>,
        val schoolsName: TextResource,
        val regionsName: TextResource
    )

    private val _resources = MutableLiveData<LoadableData<ChatSearchResources>>(NormalLoading())
    private val _event = MutableLiveData<Event>()

    private fun loadResources() {
        val gender = student?.gender ?: Gender.NONE
        val resourcesManager = RemoteTextResourcesManager(getApplication())

        val characteristicsTask = loadCharacteristicsCardsWithTranslator(getApplication(), gender)
        val schoolsTask = resourcesManager.findResource("schools")
        val regionsTask = resourcesManager.findResource("regions")

        Tasks.whenAll(schoolsTask, regionsTask, characteristicsTask).continueWith {
            val characteristicsResult = characteristicsTask.result!!
            val schools = schoolsTask.result!!
            val regions = regionsTask.result!!

            ChatSearchResources(
                characteristicsResult.translator,
                characteristicsResult.cards,
                schools,
                regions
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

    var grade: Grade = Grade.NONE
    var chosenSchool: TextContainer = Unfilled
    var chosenRegion: TextContainer = Unfilled
    var selectedCharacteristics: Set<String> = emptySet()

    val resources: LiveData<LoadableData<ChatSearchResources>> = _resources
    val event: LiveData<Event> = _event

    fun searchPressed() {
        val resources = (resources.value as? Loaded)?.data
            ?: error("pressed search before done loading resources")

        when (val region = chosenRegion) {
            is Filled -> {
                if (region.text !in resources.regionsName.getAll()) {
                    _event.value = Event.DisplayFailure(R.string.chat_search_wrong_region_message)
                    return
                }
            }
        }

        when(val school = chosenSchool) {
            is Filled ->{
                if (school.text !in resources.schoolsName.getAll()) {
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
