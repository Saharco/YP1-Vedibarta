package com.technion.vedibarta.data

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.technion.vedibarta.POJOs.CategoryCard
import com.technion.vedibarta.POJOs.Gender
import com.technion.vedibarta.utilities.VedibartaActivity
import com.technion.vedibarta.utilities.resourcesManagement.MultilingualTextResource
import com.technion.vedibarta.utilities.resourcesManagement.RemoteTextResourcesManager
import com.technion.vedibarta.utilities.resourcesManagement.TextResource

object StudentResources {
    private lateinit var _characteristicsTranslator: LiveData<MultilingualTextResource>
    private lateinit var _characteristicsCardList: List<CategoryCard>
    private lateinit var _hobbiesTranslator: LiveData<MultilingualTextResource>
    private lateinit var _hobbyCardList: List<CategoryCard>
    private lateinit var _schools: TextResource
    private lateinit var _regions: TextResource

    val gender = MutableLiveData(VedibartaActivity.student?.gender ?: Gender.NONE)

    val characteristicsTranslator by lazy { _characteristicsTranslator }
    val characteristicsCardList by lazy { _characteristicsCardList }
    val hobbiesTranslator by lazy { _hobbiesTranslator }
    val hobbyCardList by lazy { _hobbyCardList }
    val schools by lazy { _schools }
    val regions by lazy { _regions }

    fun load(application: Application): Task<Unit> {
        val resourcesManager = RemoteTextResourcesManager(application)

        val characteristicsCardsTask = loadCharacteristicsCards(application)
        val translatorMaleTask = loadCharacteristicsTranslator(application, Gender.MALE)
        val translatorFemaleTask = loadCharacteristicsTranslator(application, Gender.FEMALE)
        val hobbiesTask = loadHobbiesCardsWithTranslator(application)
        val schoolsTask = resourcesManager.findResource("schools")
        val regionsTask = resourcesManager.findResource("regions")

        val taskAll = Tasks.whenAll(
            characteristicsCardsTask,
            translatorMaleTask,
            translatorFemaleTask,
            hobbiesTask,
            schoolsTask,
            regionsTask
        )

        return taskAll.continueWith {
            _characteristicsTranslator = Transformations.map(gender) {
                when(it!!) {
                    Gender.NONE, Gender.MALE -> translatorMaleTask.result!!
                    Gender.FEMALE -> translatorFemaleTask.result!!
                }
            }
            _characteristicsCardList = characteristicsCardsTask.result!!
            _hobbiesTranslator = MutableLiveData(hobbiesTask.result!!.translator)
            _hobbyCardList = hobbiesTask.result!!.cards
            _schools = schoolsTask.result!!
            _regions = regionsTask.result!!
        }
    }
}