package com.technion.vedibarta.utilities.resourcesManagement

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.gms.tasks.Tasks
import com.google.firebase.storage.FirebaseStorage
import com.technion.vedibarta.POJOs.Gender
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RemoteResourcesManagerTest {
    private val storage = FirebaseStorage.getInstance().reference.child("/tests/RemoteResourcesManager")
    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val preferences = context.getSharedPreferences("test", Context.MODE_PRIVATE)

    @Before
    @After
    fun cleanPreferences() {
        preferences.edit { clear() }
    }

    @Test
    fun gettingRemoteResourcesWithoutPreferences() {
        val resource = Tasks.await(
                RemoteResourcesManager(context, storage, preferences)
                        .findMultilingualResource("religions")
        )

        val result = resource.getAll()

        assertEquals(listOf("Jewish", "Christian", "Muslim"), result)
    }

    @Test
    fun gettingRemoteResourcesWithDefaultPreferences() {
        preferences.edit {
            putString("Lang", "EN")
            putString("Gender", "MALE")
        }

        val resource = Tasks.await(
            RemoteResourcesManager(context, storage, preferences)
                .findMultilingualResource("religions")
        )

        val result = resource.getAll()

        assertEquals(listOf("Jewish", "Christian", "Muslim"), result)
    }

    @Test
    fun gettingRemoteResourcesWithDefaultAndNonDefaultPreferences() {
        preferences.edit {
            putString("Lang", "HE")
            putString("Gender", "MALE")
        }

        val resource = Tasks.await(
            RemoteResourcesManager(context, storage, preferences)
                .findMultilingualResource("religions")
        )

        val result = resource.getAll()

        assertEquals(listOf("יהודי", "נוצרי", "מוסלמי"), result)
    }

    @Test
    fun gettingBaseLanguageWithNoPreferences() {
        val resource = Tasks.await(
            RemoteResourcesManager(context, storage, preferences)
                .findMultilingualResource("religions")
        )

        val result = resource.getAllBase()

        assertEquals(listOf("JEWISH", "CHRISTIAN", "MUSLIM"), result)
    }

    @Test
    fun gettingBaseLanguageWithPreferences() {
        preferences.edit {
            putString("Lang", "HE")
            putString("Gender", "MALE")
        }

        val resource = Tasks.await(
            RemoteResourcesManager(context, storage, preferences)
                .findMultilingualResource("religions")
        )

        val result = resource.getAllBase()

        assertEquals(listOf("JEWISH", "CHRISTIAN", "MUSLIM"), result)
    }

    @Test
    fun overridingGenderReturnsCorrectResource() {
        preferences.edit {
            putString("Lang", "HE")
            putString("Gender", "MALE")
        }

        val resource = Tasks.await(
            RemoteResourcesManager(context, storage, preferences)
                .findMultilingualResource("religions", gender = Gender.FEMALE)
        )

        val result = resource.getAll()

        assertEquals(listOf("יהודיה", "נוצריה", "מוסלמית"), result)
    }

    @Test
    fun addingGenderReturnsCorrectResource() {
        preferences.edit {
            putString("Lang", "HE")
        }

        val resource = Tasks.await(
            RemoteResourcesManager(context, storage, preferences)
                .findMultilingualResource("religions", gender = Gender.FEMALE)
        )

        val result = resource.getAll()

        assertEquals(listOf("יהודיה", "נוצריה", "מוסלמית"), result)
    }
}
