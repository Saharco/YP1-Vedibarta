package com.technion.vedibarta.utilities.filesCaching

import android.content.Context
import java.io.File

class FilesCache(context: Context, relativePath: String) {
    private val folder = context.filesDir.resolve(relativePath)
    private val filesMap = filesMaps.getOrPut(relativePath) { mutableMapOf() }

    companion object {
        // A set of cache folders that were reset
        private var resetSet = mutableSetOf<String>()
        // A map from cache folders to their files map
        private var filesMaps = mutableMapOf<String, MutableMap<String, File>>()
    }

    init {
        if (relativePath !in resetSet) {
            clear()
            resetSet.add(relativePath)
        }

        if (!folder.isDirectory)
            folder.mkdirs()
    }

    operator fun set(fileName: String, file: File) = filesMap.set(fileName, file)

    fun newFile(fileName: String): File {
        val file = File(folder, fileName).also { it.createNewFile() }
        file.deleteOnExit()

        return file
    }

    operator fun contains(fileName: String) = fileName in filesMap

    operator fun get(fileName: String) = filesMap[fileName]

    fun deleteFile(fileName: String) = filesMap.remove(fileName)?.delete()
    fun deleteFile(file: File) = filesMap.values.remove(file)

    fun clear() = folder.deleteRecursively()
}

fun FilesCache.getOrNew(fileName: String) = get(fileName) ?: newFile(fileName)
