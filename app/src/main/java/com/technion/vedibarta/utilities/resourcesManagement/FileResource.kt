package com.technion.vedibarta.utilities.resourcesManagement

import java.io.File

class FileResource
internal constructor(
    private val file: File
) : Resource {
    private val closeListeners = mutableListOf<() -> Unit>()

    internal fun addOnCloseListener(listener: () -> Unit) {
        closeListeners.add(listener)
    }

    override fun getAll(): List<String> = file.readLines()

    override fun close() = closeListeners.forEach { it() }
}

class MultilingualFileResource
internal constructor(
    userResource: FileResource,
    private val baseResource: FileResource
) : Resource by userResource, MultilingualResource {

    constructor(userFile: File, baseFile: File): this(
        FileResource(userFile),
        FileResource(baseFile)
    )

    override fun getAllBase(): List<String> = baseResource.getAll()
}
