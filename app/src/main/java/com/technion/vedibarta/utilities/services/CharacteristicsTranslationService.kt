package com.technion.vedibarta.utilities.services

import android.content.Context
import com.technion.vedibarta.POJOs.Gender

enum class Languages {
    BASE, HEBREW, ARABIC
}

enum class StringType {
    CHARACTERISTICS, HOBBIES
}

enum class HobbyType{
    HOBBY, CATEGORY
}


fun String.translate(context: Context): TranslationServiceFactory {
    return TranslationServiceFactory(arrayOf(this).asIterable(), context)
}

fun Iterable<String>.translate(context: Context): TranslationServiceFactory {
    return TranslationServiceFactory(this, context)
}

fun Array<String>.translate(context: Context): TranslationServiceFactory {
    return TranslationServiceFactory(this.asIterable(), context)
}

class TranslationServiceFactory(
    private val strings: Iterable<String>,
    private val context: Context
) {
    fun characteristics(): CharacteristicsTranslationService {
        return CharacteristicsTranslationService(strings, context, StringType.CHARACTERISTICS)
    }

    fun hobbies(type: HobbyType = HobbyType.HOBBY): HobbiesTranslationService {
        return HobbiesTranslationService(strings, context, StringType.HOBBIES, type)
    }
}

class CharacteristicsTranslationService(
    private val string: Iterable<String>,
    context: Context,
    type: StringType
) {
    private val resourcesManager: ResourceManager = ResourceManager(context, type)
    private lateinit var resourceFrom: Array<String>
    private lateinit var resourceTo: Array<String>

    fun to(
        languageTo: Languages,
        genderTo: Gender = Gender.MALE
    ): CharacteristicsTranslationService {
        resourceTo = resourcesManager.resourcesMap["${languageTo}_$genderTo"]!!
        return this
    }

    fun from(
        languageFrom: Languages,
        genderFrom: Gender = Gender.MALE
    ): CharacteristicsTranslationService {
        resourceFrom = resourcesManager.resourcesMap["${languageFrom}_$genderFrom"]!!
        return this
    }

    fun execute(): Array<String> {
        return string.map { str ->
            resourceTo[resourceFrom.indexOf(str)]
        }.toTypedArray()
    }

}

class HobbiesTranslationService(
    private val string: Iterable<String>,
    context: Context,
    type: StringType,
    private val hobbyType: HobbyType
) {
    private val resourcesManager: ResourceManager = ResourceManager(context, type)
    private lateinit var resourceFrom: Array<String>
    private lateinit var resourceTo: Array<String>

    fun to(
        languageTo: Languages
    ): HobbiesTranslationService {
        resourceTo = resourcesManager.resourcesMap["${languageTo}_$hobbyType"]!!
        return this
    }

    fun from(
        languageFrom: Languages
    ): HobbiesTranslationService {
        resourceFrom = resourcesManager.resourcesMap["${languageFrom}_$hobbyType"]!!
        return this
    }

    fun execute(): Array<String> {
        return string.map { str ->
            resourceTo[resourceFrom.indexOf(str)]
        }.toTypedArray()
    }
}


