package com.technion.vedibarta.utilities.resourcesManagement

data class Translation(
    val inCurrent: String,
    val inBase: String
)

private class ListTextResource(private val list: List<String>) : TextResource {
    override fun getAll(): List<String> = list
}

private class ListMultilingualTextResource(private val list: List<Translation>) : MultilingualTextResource {
    override fun getAll(): List<String> = list.map { it.inCurrent }
    override fun getAllBase(): List<String> = list.map { it.inBase }
}

fun textResourceOf(list: List<String>): TextResource = ListTextResource(list)
fun textResourceOf(vararg list: String): TextResource = textResourceOf(list.toList())

fun multilingualTextResourceOf(list: List<Translation>): MultilingualTextResource = ListMultilingualTextResource(list)
fun multilingualTextResourceOf(vararg list: Translation): MultilingualTextResource = multilingualTextResourceOf(list.toList())