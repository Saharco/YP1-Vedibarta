package com.technion.vedibarta.data

import android.app.Application
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.technion.vedibarta.utilities.resourcesManagement.RemoteTextResourcesManager
import com.technion.vedibarta.utilities.resourcesManagement.TextResource

object TeacherResources {
    private lateinit var _schoolCharacteristics: CardWithTranslator
    private lateinit var _subjects: CardWithTranslator
    private lateinit var _schools: TextResource
    private lateinit var _regions: TextResource

    val schoolCharacteristics by lazy { _schoolCharacteristics }
    val subjects: CardWithTranslator by lazy { _subjects }
    val schools: TextResource by lazy { _schools }
    val regions: TextResource by lazy { _regions }

    fun load(application: Application): Task<Unit> {
        val resourcesManager = RemoteTextResourcesManager(application)

        val characteristicsTask = loadSchoolCharacteristics(application)
        val subjectsTask = loadSubjects(application)
        val schoolsTask = resourcesManager.findResource("schools")
        val regionsTask = resourcesManager.findResource("regions")

        val taskAll = Tasks.whenAll(
            characteristicsTask,
            subjectsTask,
            schoolsTask,
            regionsTask
        )

        return taskAll.continueWith {
            _schoolCharacteristics = characteristicsTask.result!!
            _subjects = subjectsTask.result!!
            _schools = schoolsTask.result!!
            _regions = regionsTask.result!!
        }
    }
}