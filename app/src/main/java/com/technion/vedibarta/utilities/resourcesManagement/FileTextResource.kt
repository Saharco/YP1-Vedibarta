package com.technion.vedibarta.utilities.resourcesManagement

import java.io.File

class FileTextResource
internal constructor(
    internal val file: File
) : TextResource {
    override fun getAll(): List<String> = file.readLines()
}

class MultilingualFileTextResource
internal constructor(
    internal val userResource: FileTextResource,
    internal val baseResource: FileTextResource
) : TextResource by userResource, MultilingualTextResource {

    constructor(userFile: File, baseFile: File): this(
        FileTextResource(userFile),
        FileTextResource(baseFile)
    )

    override fun getAllBase(): List<String> = baseResource.getAll()
}
