package com.technion.vedibarta.utilities.resourcesManagement

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.technion.vedibarta.POJOs.Gender
import com.technion.vedibarta.utilities.extensions.GENDER_KEY
import java.io.File

val RESOURCES_REFERENCE = FirebaseStorage.getInstance().reference.child("resources")

class RemoteResourcesManager(
    private val context: Context,
    private val storageReference: StorageReference = RESOURCES_REFERENCE,
    private val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
) : ResourcesManager {

    override fun findResource(name: String, gender: Gender?): Task<out Resource> =
        findResourceAux(name, gender)

    override fun findMultilingualResource(name: String, gender: Gender?): Task<MultilingualResource> {
        val baseRef = storageReference.child(name).child("base")
        var userRes: FileResource? = null
        var baseRes: FileResource? = null

        val task1 = findResourceAux(name, gender)
            .continueWith { userRes = it.result!! }
        val task2 = fetchResource(baseRef).continueWith { baseRes = it.result!! }

        return Tasks.whenAll(task1, task2).continueWith<MultilingualResource> {
            MultilingualFileResource(userRes!!, baseRes!!)
        }.addOnFailureListener {
            userRes?.close()
            baseRes?.close()
        }
    }

    private fun findResourceAux(name: String, gender: Gender?): Task<out FileResource> {
        val folder = storageReference.child(name)
        val preferencesResolver = PreferencesResolver(getPreferenceMap(preferences, gender))

        return folder.listAll().continueWithTask { task ->
            val filesList = task.result!!.items.map { it.name }
                .toMutableList().apply { remove("base") }
            val chosenFile = preferencesResolver.resolve(filesList)

            fetchResource(folder.child(chosenFile), "$name-user")
        }
    }

    private fun fetchResource(
        fileReference: StorageReference,
        name: String = fileReference.name
    ): Task<FileResource> {
        val file = File.createTempFile(name.replace("/", "_"), null, context.filesDir)

        return fileReference.getFile(file).continueWith {
            file.setReadOnly()

            FileResource(file).apply {
                addOnCloseListener { file.delete() }
            }
        }.addOnFailureListener {
            file.delete()
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