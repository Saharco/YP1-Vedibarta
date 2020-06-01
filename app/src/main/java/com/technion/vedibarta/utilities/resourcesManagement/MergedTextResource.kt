package com.technion.vedibarta.utilities.resourcesManagement

class MergedTextResource(
    private val resources: List<TextResource>
) : TextResource {

    override fun getAll(): List<String> = resources.flatMap { it.getAll() }
}

class MergedMultilingualTextResources(
    private val resources: List<MultilingualTextResource>
) : MultilingualTextResource {

    override fun getAll(): List<String> = resources.flatMap { it.getAll() }
    override fun getAllBase(): List<String> = resources.flatMap { it.getAllBase() }
}

fun merge(resources: List<TextResource>) = MergedTextResource(resources)
fun merge(resources: List<MultilingualTextResource>) = MergedMultilingualTextResources(resources)

fun merge(vararg resource: TextResource) = merge(resource.toList())
fun merge(vararg resource: MultilingualTextResource) = merge(resource.toList())
