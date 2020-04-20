package com.technion.vedibarta.utillities.questionGenerator


import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.gms.tasks.Tasks
import com.google.firebase.storage.FirebaseStorage
import com.technion.vedibarta.POJOs.Gender
import com.technion.vedibarta.utilities.questionGenerator.QuestionGeneratorFactory
import com.technion.vedibarta.utilities.resourcesManagement.RemoteResourcesManager
import com.technion.vedibarta.utilities.resourcesManagement.findMultilingualResources
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QuestionGeneratorManagerTest {
    private val storage = FirebaseStorage.getInstance().reference.child("/tests/QuestionGenerator")
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val preferences = context.getSharedPreferences("test", Context.MODE_PRIVATE)
    private val userHobbies: MutableList<String> = mutableListOf()
    private val partnerHobbies: MutableList<String> = mutableListOf()

    @Before
    @After
    fun cleanHobbies() {
        userHobbies.clear()
        partnerHobbies.clear()
    }

    @Before
    fun clearPreferences() {
        preferences.edit { clear() }
        preferences.edit {
            putString("Lang", "HE")
        }
    }

    @Test
    fun creatingMutualQuestionGenerator() {
        userHobbies.addAll(arrayOf("music, tennis, food"))
        partnerHobbies.addAll(arrayOf("tennis, food"))
        assertNotNull(QuestionGeneratorFactory(context, userHobbies, partnerHobbies, storageReference = storage, preferences = preferences).getGenerator())
    }

    @Test
    fun creatingDistinctQuestionGenerator() {
        userHobbies.addAll(arrayOf("music"))
        partnerHobbies.addAll(arrayOf("tennis"))
        assertNotNull(QuestionGeneratorFactory(context, userHobbies, partnerHobbies, storageReference = storage, preferences = preferences).getGenerator())
    }

    @Test
    fun createsAGeneratorEvenWithoutHobbies() {
        assertNotNull(QuestionGeneratorFactory(context, userHobbies, partnerHobbies, storageReference = storage, preferences = preferences).getGenerator())
    }

    @Test
    fun returnsMutualHobbies() {
        userHobbies.addAll(arrayOf("music, tennis, food"))
        partnerHobbies.addAll(arrayOf("tennis, food"))
        val mutualGenerator =
            QuestionGeneratorFactory(context, userHobbies, partnerHobbies, storageReference = storage, preferences = preferences).getGenerator()

        assertArrayEquals(
            arrayOf("tennis, food"),
            mutualGenerator.getMutualHobbies().toTypedArray()
        )
    }

    @Test
    fun returnsEmptyListWhenThereAreNoMutualHobbies() {
        userHobbies.addAll(arrayOf("music"))
        partnerHobbies.addAll(arrayOf("tennis"))
        val generator =
            QuestionGeneratorFactory(context, userHobbies, partnerHobbies, storageReference = storage, preferences = preferences).getGenerator()
        assertArrayEquals(emptyArray<String>(), generator.getMutualHobbies().toTypedArray())
    }

    @Test
    fun returnsMutualCategories() {
        userHobbies.addAll(arrayOf("music, tennis, food"))
        partnerHobbies.addAll(arrayOf("tennis, food"))
        val mutualCategories =
            QuestionGeneratorFactory(context, userHobbies, partnerHobbies, storageReference = storage, preferences = preferences).getGenerator()
                .getMutualCategories()
        mutualCategories.onSuccessTask {
            assertArrayEquals(arrayOf("sports, pastime"), it!!.toTypedArray())
            Tasks.call { emptyList<String>() }
        }
            .addOnFailureListener { assertFalse(true) }
    }

    @Test
    fun returnsEmptyListWhenThereAreNoMutualCategories() {
        userHobbies.addAll(arrayOf("music"))
        partnerHobbies.addAll(arrayOf("tennis"))
        val mutualCategories =
            QuestionGeneratorFactory(context, userHobbies, partnerHobbies, storageReference = storage, preferences = preferences).getGenerator()
                .getMutualCategories()
        mutualCategories.onSuccessTask {
            assertArrayEquals(emptyArray(), it!!.toTypedArray())
            Tasks.call { emptyList<String>() }
        }
            .addOnFailureListener { assertFalse(true) }
    }

    @Test
    fun returnsQuestionsBasedOnMutualHobbies() {
        userHobbies.addAll(arrayOf("music, tennis, food"))
        partnerHobbies.addAll(arrayOf("tennis, food"))
        val questions =
            QuestionGeneratorFactory(context, userHobbies, partnerHobbies, storageReference = storage, preferences = preferences).getGenerator()
                .getQuestionsBasedOnMutualHobbies()
        val downloadedQuestions = RemoteResourcesManager(context, storage, preferences)
            .findMultilingualResources("/questions/tennis", "/questions/food")
        Tasks.whenAll(questions, downloadedQuestions).onSuccessTask {
            assertArrayEquals(arrayOf("sports, pastime"), questions.result!!.keys.toTypedArray())
            downloadedQuestions.result!!.forEachIndexed { index, multilingualResource ->
                assertArrayEquals(
                    multilingualResource.getAll().slice(1 until multilingualResource.getAll().size).toTypedArray(),
                    questions.result!![questions.result!!.keys.toTypedArray()[index]]!!.toTypedArray()
                )
            }
            Tasks.call { emptyList<String>() }
        }
            .addOnFailureListener { assertFalse(true) }
    }


    @Test
    fun returnsQuestionsBasedOnMutualCategories() {
        userHobbies.addAll(arrayOf("music, tennis, food"))
        partnerHobbies.addAll(arrayOf("tennis, food"))
        val questions =
            QuestionGeneratorFactory(context, userHobbies, partnerHobbies, storageReference = storage, preferences = preferences).getGenerator()
                .getQuestionsBasedOnMutualHobbies()
        val downloadedQuestions = RemoteResourcesManager(context, storage, preferences)
            .findMultilingualResources("/questions/sports", "/questions/pastime")
        Tasks.whenAll(questions, downloadedQuestions).onSuccessTask {
            assertArrayEquals(arrayOf("sports, pastime"), questions.result!!.keys.toTypedArray())
            downloadedQuestions.result!!.forEachIndexed { index, multilingualResource ->
                assertArrayEquals(
                    multilingualResource.getAll().toTypedArray(),
                    questions.result!![questions.result!!.keys.toTypedArray()[index]]!!.toTypedArray()
                )
            }
            Tasks.call { emptyList<String>() }
        }
            .addOnFailureListener { assertFalse(true) }
    }

    @Test
    fun returnsAllQuestions() {
        userHobbies.addAll(arrayOf("music"))
        partnerHobbies.addAll(arrayOf("music"))
        val generalQuestionsTask = RemoteResourcesManager(context,storage,preferences).findMultilingualResource("questions/general")
        val musicQuestionsTask = RemoteResourcesManager(context,storage,preferences).findMultilingualResource("questions/music")
        val artsCategoryTask = RemoteResourcesManager(context,storage,preferences).findMultilingualResource("questions/arts")
        val questions = QuestionGeneratorFactory(context,userHobbies,partnerHobbies, storageReference = storage, preferences = preferences).getGenerator()
            .getQuestions()
        Tasks.whenAll(generalQuestionsTask, questions, musicQuestionsTask, artsCategoryTask)
            .onSuccessTask {
                val list = generalQuestionsTask.result!!.getAll()
                val musicList = musicQuestionsTask.result!!.getAll()
                val artsList = artsCategoryTask.result!!.getAll()
                val map = mapOf<String, List<String>>("כללי" to list.slice(1 until list.size))
                val finalMap = map.plus(musicList[0] to musicList.slice(1 until musicList.size))
                    .plus(musicList[0] to artsList)
                assertEquals(finalMap, questions.result!!)
                Tasks.call { emptyList<String>() }
            }
            .addOnFailureListener { assertFalse(true) }
    }

    @Test
    fun distinctQuestionGeneratorReturnsOnlyGeneralQuestionsInCaseThereAreNoMutualCategories() {
        userHobbies.addAll(arrayOf("music"))
        partnerHobbies.addAll(arrayOf("tennis"))
        val generalQuestionsTask = RemoteResourcesManager(context,storage,preferences).findMultilingualResource("questions/general")
        val questions = QuestionGeneratorFactory(context,userHobbies,partnerHobbies, storageReference = storage, preferences = preferences).getGenerator()
            .getQuestions()
        Tasks.whenAll(generalQuestionsTask, questions)
            .onSuccessTask {
                val list = generalQuestionsTask.result!!.getAll()
                val map = "כללי" to list.slice(1 until list.size)
                assertEquals(map, questions.result!!)
                Tasks.call { emptyList<String>() }
            }
            .addOnFailureListener { assertFalse(true) }
    }

}