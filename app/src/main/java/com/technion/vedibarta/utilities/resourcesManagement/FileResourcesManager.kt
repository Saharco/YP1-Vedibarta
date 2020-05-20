package com.technion.vedibarta.utilities.resourcesManagement

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import java.io.File

interface FileResourcesManager {
    /**
     * @param name The name of the file-resource to find
     * @return a [Task] containing the wanted file
     */
    fun findResource(name: String): Task<File>

    /**
     * @param dirName the name of the directory in which file-resources will be searched
     * @return a list of names of the file-resources contained in the directory
     */
    fun listFilesInDir(dirName: String): Task<List<String>>
}

/**
 * @param dirName the name of the directory in which file-resources will be searched
 * @return a map from file names to the files contained in the directory
 */
fun FileResourcesManager.getAllInDirectory(dirName: String): Task<Map<String, File>> =
    listFilesInDir(dirName).continueWithTask { namesTask ->
        val filesName = namesTask.result!!

        Tasks.whenAllSuccess<File>(filesName.map { findResource(it) })
            .continueWith { filesTask ->
                val files = filesTask.result!!

                filesName.zip(files).toMap()
            }
    }

fun FileResourcesManager.getAllFilesInDirectory(dirName: String): Task<List<File>> =
    getAllInDirectory(dirName).continueWith {
        it.result!!.values.toList()
    }
