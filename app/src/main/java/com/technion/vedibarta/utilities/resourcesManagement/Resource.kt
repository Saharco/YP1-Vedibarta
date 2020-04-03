package com.technion.vedibarta.utilities.resourcesManagement

import java.io.Closeable

/**
 * An Interface for texts resource.
 */
interface Resource: Closeable {
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
interface MultilingualResource: Resource {

    /**
     * @return a dictionary ([List] of [String]) in the app's universal language
     */
    fun getAllBase(): List<String>
}

/**
 * @param textBase the text in the universal language to be translated
 * @return the translation of [textBase]
 */
fun MultilingualResource.toCurrentLanguage(textBase: String): String =
    getAll().atSameIndexOf(textBase, getAllBase())

/**
 * @param text the text in the universal language to be translated
 * @return the translation of [text]
 */
fun MultilingualResource.toBaseLanguage(text: String): String =
    getAllBase().atSameIndexOf(text, getAll())

private fun <T, G> List<T>.atSameIndexOf(element: G, other: List<G>): T =
    this[other.indexOf(element)]
