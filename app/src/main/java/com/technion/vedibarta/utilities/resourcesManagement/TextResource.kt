package com.technion.vedibarta.utilities.resourcesManagement

/**
 * An Interface for texts resource.
 */
interface TextResource {
    /**
     * @return all the texts of the resource
     */
    fun getAll(): List<String>
}

/**
 * An Interface for multilingual texts resources.
 *
 * Use [getAll] to get the texts in the user's language and [getAllBase] to get the texts in the
 * app's universal language. Each text received by [getAll] correspond to the text received by
 * [getAllBase] at the same index. Use [toCurrentLanguage] and [toBaseLanguage] in order to perform
 * translations from one of the languages to the other.
 */
interface MultilingualTextResource: TextResource {

    /**
     * @return a dictionary ([List] of [String]) in the app's universal language
     */
    fun getAllBase(): List<String>
}

/**
 * @param textBase the text in the universal language to be translated
 * @return the translation of [textBase]
 */
fun MultilingualTextResource.toCurrentLanguage(textBase: String): String =
    getAll().atSameIndexOf(textBase, getAllBase())

fun MultilingualTextResource.toCurrentLanguage(textBase: Iterable<String>): Iterable<String> =
    textBase.map { getAll().atSameIndexOf(it, getAllBase()) }

fun MultilingualTextResource.toCurrentLanguage(textBase: Array<String>): Array<String> =
    textBase.map { getAll().atSameIndexOf(it, getAllBase()) }.toTypedArray()

/**
 * @param text the text in the universal language to be translated
 * @return the translation of [text]
 */
fun MultilingualTextResource.toBaseLanguage(text: String): String =
    getAllBase().atSameIndexOf(text, getAll())

fun MultilingualTextResource.toBaseLanguage(text: Iterable<String>): Iterable<String> =
    text.map { getAllBase().atSameIndexOf(it, getAll())}

fun MultilingualTextResource.toBaseLanguage(text: Array<String>): Array<String> =
    text.map { getAllBase().atSameIndexOf(it, getAll())}.toTypedArray()

private fun <T, G> List<T>.atSameIndexOf(element: G, other: List<G>): T =
    this[other.indexOf(element)]
