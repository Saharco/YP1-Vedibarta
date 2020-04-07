package com.technion.vedibarta.utilities.resourcesManagement

import java.io.File

class FileResource
internal constructor(
    internal val file: File
) : Resource {
    override fun getAll(): List<String> = file.readLines()
}

class MultilingualFileResource
internal constructor(
    internal val userResource: FileResource,
    internal val baseResource: FileResource
) : Resource by userResource, MultilingualResource {

    constructor(userFile: File, baseFile: File): this(
        FileResource(userFile),
        FileResource(baseFile)
    )

    override fun getAllBase(): List<String> = baseResource.getAll()
}
