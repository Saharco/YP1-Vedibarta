package com.technion.vedibarta.utillities.questionGenerator


import android.content.Context
import androidx.core.content.edit
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.gms.tasks.Tasks
import com.google.firebase.storage.FirebaseStorage
import com.technion.vedibarta.utilities.questionGenerator.QuestionGeneratorFactory
import com.technion.vedibarta.utilities.resourcesManagement.RemoteTextResourcesManager
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
        assertNotNull(
            QuestionGeneratorFactory(
                context,
                userHobbies,
                partnerHobbies,
                storageReference = storage,
                preferences = preferences
            ).getGenerator()
        )
    }

    @Test
    fun creatingDistinctQuestionGenerator() {
        userHobbies.addAll(arrayOf("music"))
        partnerHobbies.addAll(arrayOf("tennis"))
        assertNotNull(
            QuestionGeneratorFactory(
                context,
                userHobbies,
                partnerHobbies,
                storageReference = storage,
                preferences = preferences
            ).getGenerator()
        )
    }

    @Test
    fun createsAGeneratorEvenWithoutHobbies() {
        assertNotNull(
            QuestionGeneratorFactory(
                context,
                userHobbies,
                partnerHobbies,
                storageReference = storage,
                preferences = preferences
            ).getGenerator()
        )
    }

    @Test
    fun returnsMutualHobbies() {
        userHobbies.addAll(arrayOf("music", "tennis", "food"))
        partnerHobbies.addAll(arrayOf("tennis", "food"))
        val mutualGenerator =
            QuestionGeneratorFactory(
                context,
                userHobbies,
                partnerHobbies,
                storageReference = storage,
                preferences = preferences
            ).getGenerator()

        assertArrayEquals(
            arrayOf("tennis", "food"),
            mutualGenerator.getMutualHobbies().toTypedArray()
        )
    }

    @Test
    fun returnsEmptyListWhenThereAreNoMutualHobbies() {
        userHobbies.addAll(arrayOf("music"))
        partnerHobbies.addAll(arrayOf("tennis"))
        val generator =
            QuestionGeneratorFactory(
                context,
                userHobbies,
                partnerHobbies,
                storageReference = storage,
                preferences = preferences
            ).getGenerator()
        assertArrayEquals(emptyArray<String>(), generator.getMutualHobbies().toTypedArray())
    }

    @Test
    fun returnsMutualCategories() {
        userHobbies.addAll(arrayOf("music", "tennis", "food"))
        partnerHobbies.addAll(arrayOf("tennis", "food"))
        val mutualCategories =
            QuestionGeneratorFactory(
                context,
                userHobbies,
                partnerHobbies,
                storageReference = storage,
                preferences = preferences
            ).getGenerator()
                .getMutualCategories()
        Tasks.await(mutualCategories)
        assertArrayEquals(arrayOf("ספורט", "בילויים"), mutualCategories.result!!.toTypedArray())
    }

    @Test
    fun returnsEmptyListWhenThereAreNoMutualCategories() {
        userHobbies.addAll(arrayOf("music"))
        partnerHobbies.addAll(arrayOf("tennis"))

        val mutualCategories = Tasks.await(
            QuestionGeneratorFactory(
                context,
                userHobbies,
                partnerHobbies,
                storageReference = storage,
                preferences = preferences
            ).getGenerator()
                .getMutualCategories()
        )

        assertArrayEquals(emptyArray(), mutualCategories.toTypedArray())
    }

    @Test
    fun returnsQuestionsBasedOnMutualHobbies() {
        userHobbies.addAll(arrayOf("music", "tennis", "food"))
        partnerHobbies.addAll(arrayOf("tennis", "food"))

        val questions = Tasks.await(
            QuestionGeneratorFactory(
                context,
                userHobbies,
                partnerHobbies,
                storageReference = storage,
                preferences = preferences
            ).getGenerator()
                .getQuestionsBasedOnMutualHobbies()
        )
        val downloadedQuestions = Tasks.await(
            RemoteTextResourcesManager(context, storage, preferences)
                .findMultilingualResources("/questions/tennis", "/questions/food")
        )

        assertArrayEquals(arrayOf("ספורט", "בילויים"), questions.keys.toTypedArray())
        downloadedQuestions.forEachIndexed { index, multilingualResource ->
            assertArrayEquals(
                multilingualResource.getAll().slice(1 until multilingualResource.getAll().size)
                    .toTypedArray(),
                questions[questions.keys.toTypedArray()[index]]?.toTypedArray()
            )
        }
    }


    @Test
    fun returnsQuestionsBasedOnMutualCategories() {
        userHobbies.addAll(arrayOf("music", "tennis", "food"))
        partnerHobbies.addAll(arrayOf("tennis", "food"))
        val questions = Tasks.await(
            QuestionGeneratorFactory(
                context,
                userHobbies,
                partnerHobbies,
                storageReference = storage,
                preferences = preferences
            ).getGenerator()
                .getQuestionsBasedOnMutualCategories()
        )
        val downloadedQuestions = Tasks.await(
            RemoteTextResourcesManager(context, storage, preferences)
                .findMultilingualResources("/questions/sports", "/questions/pastime")
        )
        assertArrayEquals(
            arrayOf("ספורט", "בילויים"),
            questions.keys.toTypedArray()
        )
        downloadedQuestions.forEachIndexed { index, multilingualResource ->
            assertArrayEquals(
                multilingualResource.getAll().toTypedArray(),
                questions[questions.keys.toTypedArray()[index]]?.toTypedArray()
            )
        }
    }

    @Test
    fun returnsAllQuestions() {
        userHobbies.addAll(arrayOf("music"))
        partnerHobbies.addAll(arrayOf("music"))
        val generalQuestionsTask = Tasks.await(RemoteTextResourcesManager(
            context,
            storage,
            preferences
        ).findMultilingualResource("questions/general"))
        val musicQuestionsTask = Tasks.await(RemoteTextResourcesManager(
            context,
            storage,
            preferences
        ).findMultilingualResource("questions/music"))
        val artsCategoryTask = Tasks.await(RemoteTextResourcesManager(
            context,
            storage,
            preferences
        ).findMultilingualResource("questions/arts"))
        val questions = Tasks.await(QuestionGeneratorFactory(
            context,
            userHobbies,
            partnerHobbies,
            storageReference = storage,
            preferences = preferences
        ).getGenerator()
            .getQuestions())
        val list = generalQuestionsTask.getAll()
        val musicList = musicQuestionsTask.getAll()
        val artsList = artsCategoryTask.getAll()
        val map = mapOf("כללי" to list.slice(1 until list.size))
        val finalMap = map.plus(musicList[0] to musicList.slice(1 until musicList.size))
            .plus(musicList[0] to artsList)
        assertEquals(finalMap, questions)
    }

    @Test
    fun distinctQuestionGeneratorReturnsOnlyGeneralQuestionsInCaseThereAreNoMutualCategories() {
        userHobbies.addAll(arrayOf("music"))
        partnerHobbies.addAll(arrayOf("tennis"))
        val generalQuestionsTask = Tasks.await(RemoteTextResourcesManager(
            context,
            storage,
            preferences
        ).findMultilingualResource("questions/general"))
        val questions = Tasks.await(QuestionGeneratorFactory(
            context,
            userHobbies,
            partnerHobbies,
            storageReference = storage,
            preferences = preferences
        ).getGenerator()
            .getQuestions())

        val list = generalQuestionsTask.getAll()
        val map = mapOf("כללי" to list.slice(1 until list.size))
        assertEquals(map, questions)
    }

}