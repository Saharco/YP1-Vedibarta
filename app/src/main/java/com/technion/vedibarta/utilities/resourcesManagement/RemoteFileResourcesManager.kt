package com.technion.vedibarta.utilities.resourcesManagement

import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.technion.vedibarta.utilities.filesCaching.FilesCache
import java.io.File

private const val CACHE_RELATIVE_PATH = "FILE_RESOURCES"
private val RESOURCES_REFERENCE = FirebaseStorage.getInstance().reference.child("resources")

class RemoteFileResourcesManager(
    context: Context,
    private val storageReference: StorageReference = RESOURCES_REFERENCE
): FileResourcesManager {
    private val cache = FilesCache(
        context,
        CACHE_RELATIVE_PATH
    )

    override fun findResource(name: String): Task<File> =
        when (val localFileName = name.replace('/', '_')) {
            in cache -> Tasks.call { cache[localFileName]!! }
            else -> fetchFromDatabase(storageReference.child(name), localFileName)
        }

    // Fetches the wanted file from the database and creates the wanted resource.
    private fun fetchFromDatabase(
        fileReference: StorageReference,
        localFileName: String
    ): Task<File> {
        val localFile = cache.newFile(localFileName)

        return fileReference.getFile(localFile)
            .continueWith {
                localFile.setReadOnly()
                cache[localFileName] = localFile
                localFile
            }.addOnFailureListener {
                cache.deleteFile(localFileName)
            }
    }

    override fun listFilesInDir(dirName: String): Task<List<String>> =
        storageReference.child(dirName).listAll().continueWith { task ->
            task.result!!.items.map { it.name }
        }
}