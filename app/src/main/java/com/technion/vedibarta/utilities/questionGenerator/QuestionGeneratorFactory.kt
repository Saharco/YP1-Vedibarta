package com.technion.vedibarta.utilities.questionGenerator

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.storage.StorageReference
import com.technion.vedibarta.utilities.resourcesManagement.*

class QuestionGeneratorFactory(
    context: Context,
    private val userHobbiesList: List<String>,
    private val partnerHobbiesList: List<String>,
    storageReference: StorageReference = RESOURCES_REFERENCE,
    preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
) {

    private val mutualHobbies: List<String> = userHobbiesList.intersect(partnerHobbiesList).toList()
    private val resourcesManager = RemoteTextResourcesManager(context, storageReference, preferences)

    fun getGenerator(): QuestionGeneratorManager {
        return if (mutualHobbies.isEmpty()) DistinctQuestionGenerator(
            userHobbiesList,
            partnerHobbiesList,
            resourcesManager
        ) else MutualQuestionGenerator(mutualHobbies, resourcesManager)
    }
}


private class MutualQuestionGenerator(
    private val mutualHobbies: List<String>,
    private val resourcesManager: RemoteTextResourcesManager
) : QuestionGeneratorManager {

    private val generalQuestionsTask =
        this.resourcesManager.findMultilingualResource("questions/general")
    private var mutualHobbiesTask: Task<Map<String, List<String>>>
    private var mutualCategoriesTask: Task<Map<String, List<String>>>

    // should be downloaded already
    private val hobbiesCategoriesTask =
        resourcesManager.findMultilingualResource("hobbies/categories")

    init {
        mutualHobbiesTask = downloadMutualHobbiesQuestions()

        mutualCategoriesTask = downloadMutualCategoriesQuestions()
    }

    private fun downloadMutualHobbiesQuestions(): Task<Map<String, List<String>>> {
        val mutualHobbiesFirebase = mutualHobbies.map { hobby -> "questions/$hobby" }
        return resourcesManager.findMultilingualResources(*mutualHobbiesFirebase.toTypedArray())
            .continueWith { hobbiesQuestionsList ->
                val questionsMap: MutableMap<String, List<String>> = mutableMapOf()
                hobbiesQuestionsList.result!!.forEach {
                    if (!questionsMap.containsKey(it.getAll()[0]))
                        questionsMap[it.getAll()[0]] = it.getAll().slice(1 until it.getAll().size)
                    else
                        questionsMap[it.getAll()[0]] = questionsMap[it.getAll()[0]]!!.plus(it.getAll().slice(1 until it.getAll().size))
                }
                return@continueWith questionsMap.toMap()
            }
    }

    private fun downloadMutualCategoriesQuestions(): Task<Map<String, List<String>>> {
        return Tasks.whenAll(mutualHobbiesTask, hobbiesCategoriesTask).onSuccessTask {
            val mutualCategories = mutualHobbiesTask.result!!.keys.map { category ->
                "questions/${hobbiesCategoriesTask.result!!.toBaseLanguage(category)}"
            }
            val questionsMap: MutableMap<String, List<String>> = mutableMapOf()
            resourcesManager.findMultilingualResources(*mutualCategories.toTypedArray())
                .continueWith { categoriesQuestionsList ->
                    categoriesQuestionsList.result!!.forEachIndexed { index, multilingualResource ->
                        questionsMap[mutualHobbiesTask.result!!.keys.toTypedArray()[index]] =
                            multilingualResource.getAll()
                    }
                    return@continueWith questionsMap.toMap()
                }
        }
    }

    override fun getMutualHobbies(): List<String> = mutualHobbies

    override fun getMutualCategories(): Task<List<String>> {
        return mutualHobbiesTask.continueWith { it.result!!.keys.toList() }
    }

    override fun getQuestionsBasedOnMutualHobbies(): Task<Map<String, List<String>>> =
        mutualHobbiesTask

    override fun getQuestionsBasedOnMutualCategories(): Task<Map<String, List<String>>> =
        mutualCategoriesTask

    override fun getQuestions(): Task<Map<String, List<String>>> {
        return Tasks.whenAll(mutualCategoriesTask, mutualHobbiesTask, generalQuestionsTask)
            .continueWith {
                val mutualHobbiesQuestionsMap = mutualHobbiesTask.result!!
                val mutualCategoriesQuestionMap = mutualCategoriesTask.result!!
                val generalQuestionList = generalQuestionsTask.result!!.getAll()
                val questionsMap: MutableMap<String, List<String>> = mutualHobbiesQuestionsMap
                    .toMutableMap()
                for (key in questionsMap.keys){
                    if (mutualCategoriesQuestionMap.containsKey(key))
                        questionsMap[key] = questionsMap[key]!!.plus(mutualCategoriesQuestionMap[key]!!.toList()).distinct()
                }
                questionsMap[generalQuestionList[0]] =generalQuestionList.slice(1 until generalQuestionList.size)

                questionsMap
            }
    }
}

private class DistinctQuestionGenerator(
    private val userHobbiesList: List<String>,
    private val partnerHobbiesList: List<String>,
    private val resourcesManager: RemoteTextResourcesManager
) : QuestionGeneratorManager {

    private val generalQuestionsTask =
        this.resourcesManager.findMultilingualResource("questions/general")
    private var mutualCategoriesTask: Task<Map<String, List<String>>>

    private val reverseHobbiesTask = resourcesManager.findResource("hobbies/reverseMap")

    // should be downloaded already
    private val hobbiesTask = resourcesManager.findMultilingualResource("hobbies/all")
    private val hobbiesCategoriesTask =
        resourcesManager.findMultilingualResource("hobbies/categories")

    init {
        mutualCategoriesTask = downloadMutualCategoriesQuestions()
    }


    private fun downloadMutualCategoriesQuestions(): Task<Map<String, List<String>>> {
        return Tasks.whenAll(hobbiesTask, hobbiesCategoriesTask, reverseHobbiesTask)
            .continueWithTask {
                val hobbies = hobbiesTask.result!!.getAllBase()
                val reverseHobbies = reverseHobbiesTask.result!!.getAll()
                val userCategories = userHobbiesList.map { reverseHobbies[hobbies.indexOf(it)] }
                val partnerCategories =
                    partnerHobbiesList.map { reverseHobbies[hobbies.indexOf(it)] }
                val mutualCategories = userCategories.intersect(partnerCategories).toList()
                if (mutualCategories.isEmpty()) return@continueWithTask Tasks.call { emptyMap<String, List<String>>() }
                val mutualCategoriesFirebase = mutualCategories.map { "questions/$it" }
                val questionsMap: MutableMap<String, List<String>> = mutableMapOf()
                return@continueWithTask resourcesManager.findMultilingualResources(*mutualCategoriesFirebase.toTypedArray())
                    .continueWith { categoriesQuestionsList ->
                        categoriesQuestionsList.result!!.forEachIndexed { index, multilingualResource ->
                            questionsMap[hobbiesCategoriesTask.result!!.toCurrentLanguage(
                                mutualCategories[index]
                            )] = multilingualResource.getAll()
                        }
                        return@continueWith questionsMap.toMap()
                    }
            }
    }

    override fun getMutualHobbies(): List<String> = emptyList()

    override fun getMutualCategories(): Task<List<String>> =
        mutualCategoriesTask.continueWith { it.result!!.keys.toList() }

    override fun getQuestionsBasedOnMutualHobbies(): Task<Map<String, List<String>>> =
        Tasks.call { emptyMap<String, List<String>>() }

    override fun getQuestionsBasedOnMutualCategories(): Task<Map<String, List<String>>> =
        mutualCategoriesTask

    override fun getQuestions(): Task<Map<String, List<String>>> {
        return Tasks.whenAll(mutualCategoriesTask, generalQuestionsTask)
            .continueWith {
                val mutualCategoriesQuestionMap = mutualCategoriesTask.result!!
                val generalQuestionList = generalQuestionsTask.result!!.getAll()
                val questionsMap = mutualCategoriesQuestionMap
                    .plus(mapOf(generalQuestionList[0] to generalQuestionList.slice(1 until generalQuestionList.size)))
                questionsMap
            }
    }
}