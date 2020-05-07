package com.technion.vedibarta.utilities.resourcesManagement

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.technion.vedibarta.POJOs.Gender
import com.technion.vedibarta.utilities.extensions.GENDER_KEY
import com.technion.vedibarta.utilities.filesCaching.FilesCache

private const val CACHE_RELATIVE_PATH = "TEXT_RESOURCES"
private val RESOURCES_REFERENCE = FirebaseStorage.getInstance().reference.child("resources")

class RemoteTextResourcesManager(
    private val context: Context,
    private val storageReference: StorageReference = RESOURCES_REFERENCE,
    private val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
) : TextResourcesManager {
    private val cache = FilesCache(
        context,
        CACHE_RELATIVE_PATH
    )

    override fun findResource(name: String, gender: Gender?): Task<out TextResource> =
        findFileResource(name, gender)

    override fun findMultilingualResource(name: String, gender: Gender?): Task<MultilingualTextResource> {
        var userRes: FileTextResource? = null
        var baseRes: FileTextResource? = null

        val task1 = findFileResource(name, gender).continueWith { userRes = it.result!! }
        val task2 = fetchBase(name).continueWith { baseRes = it.result!! }

        return Tasks.whenAll(task1, task2).continueWith<MultilingualTextResource> {
            MultilingualFileTextResource(userRes!!, baseRes!!)
        }.addOnFailureListener {
            userRes?.apply { cache.deleteFile(file) }
            baseRes?.apply { cache.deleteFile(file) }
        }
    }

    // Returns the wanted FileResource.
    private fun findFileResource(name: String, gender: Gender?): Task<out FileTextResource> {
        val folder = storageReference.child(name)
        val preferencesMap = getPreferenceMap(preferences, gender)
        val preferencesResolver = PreferencesResolver(preferencesMap)
        val localFileName = "${name.replace('/', '_')}-${preferencesMap[GENDER_KEY]}-user"

        return if (localFileName in cache)
            Tasks.call { FileTextResource(cache[localFileName]!!) }

        else folder.listAll().continueWithTask { task ->
            val filesList = task.result!!.items.map { it.name }
                .toMutableList().apply { remove("base") }
            val chosenFile = preferencesResolver.resolve(filesList)

            fetchFromDatabase(folder.child(chosenFile), localFileName)
        }
    }

    private fun fetchBase(name: String): Task<FileTextResource> {
        val baseRef = storageReference.child(name).child("base")
        val localFileName = "${name.replace('/', '_')}-base"

        return if (localFileName in cache)
            Tasks.call { FileTextResource(cache[localFileName]!!) }

        else fetchFromDatabase(baseRef, localFileName)
    }

    // Fetches the wanted file from the database and creates the wanted resource.
    private fun fetchFromDatabase(
        fileReference: StorageReference,
        localFileName: String
    ): Task<FileTextResource> {
        val localFile = cache.newFile(localFileName)

        return fileReference.getFile(localFile)
            .continueWith {
                localFile.setReadOnly()
                cache[localFileName] = localFile
                FileTextResource(localFile)
            }.addOnFailureListener {
                cache.deleteFile(localFileName)
            }
    }

    private fun getPreferenceMap(preferences: SharedPreferences, gender: Gender?): Map<String, *> {
        val map = preferences.all.toMutableMap()

        if (gender != null) {
            map[GENDER_KEY] = gender.toString()
        }

        return map
    }
}