package com.technion.vedibarta.utilities.services

import android.content.Context
import com.technion.vedibarta.POJOs.Gender

enum class Languages{
    BASE, HEBREW, ARABIC
}

enum class StringType{
    CHARACTERISTICS, HOBBIES
}


fun String.translate(context: Context)  :TranslationServiceFactory {
    return TranslationServiceFactory(arrayOf(this).asIterable(), context)
}

fun Iterable<String>.translate(context: Context)  : TranslationServiceFactory {
    return TranslationServiceFactory(this, context)
}

fun Array<String>.translate(context: Context) : TranslationServiceFactory{
    return TranslationServiceFactory(this.asIterable(), context)
}

class TranslationServiceFactory(
    private val strings: Iterable<String>,
    private val context: Context
){
    fun characteristics() : TranslationService{
        return TranslationService(strings, context, StringType.CHARACTERISTICS)
    }

    fun hobbies() : TranslationService{
        TODO()
    }
}

class TranslationService(
    private val string: Iterable<String>,
    context: Context,
    type: StringType
){
    private val resourcesManager: ResourceManager = ResourceManager(context, type)
    private lateinit var resourceFrom: Array<String>
    private lateinit var resourceTo: Array<String>

    fun to(languageTo: Languages, genderTo: Gender=Gender.MALE): TranslationService {
        resourceTo = resourcesManager.resourcesMap["${languageTo}_$genderTo"]!!
        return this
    }

    fun from(languageFrom: Languages, genderFrom: Gender=Gender.MALE): TranslationService {
        resourceFrom = resourcesManager.resourcesMap["${languageFrom}_$genderFrom"]!!
        return this
    }

    fun execute() : Array<String>{
        return string.map { str->
            resourceTo[resourceFrom.indexOf(str)]
        }.toTypedArray()
    }

}