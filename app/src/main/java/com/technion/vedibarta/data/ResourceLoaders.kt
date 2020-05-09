package com.technion.vedibarta.data

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Task
import com.technion.vedibarta.POJOs.Gender
import com.technion.vedibarta.POJOs.HobbyCard
import com.technion.vedibarta.utilities.resourcesManagement.RemoteResourcesManager
import com.technion.vedibarta.utilities.resourcesManagement.findMultilingualResources
import com.technion.vedibarta.utilities.resourcesManagement.toCurrentLanguage

typealias CategoriesMapper = Map<String, Array<String>>

fun loadCharacteristics(
    context: Context,
    gender: Gender
): Task<CategoriesMapper> {

    return RemoteResourcesManager(context)
        .findMultilingualResource("characteristics/categories")
        .continueWithTask {
            val categories = it.result!!.getAllBase()
            val categoryResource = it.result!!
            val characteristicsMap = mutableMapOf<String, Array<String>>()
            val categoryResourceList = categories.map { category -> "characteristics/category-$category" }
            RemoteResourcesManager(context)
                .findMultilingualResources(*categoryResourceList.toTypedArray(), gender = gender)
                .continueWith {
                    categories.forEachIndexed { index, category ->
                        characteristicsMap[categoryResource.toCurrentLanguage(category)] = it.result!![index].getAll().toTypedArray()
                    }
                }.continueWith {
                    characteristicsMap.toMap()
                }
        }
}

fun loadHobbies(
    context: Context
): Task<List<HobbyCard>> {

    return RemoteResourcesManager(context)
        .findMultilingualResource("hobbies/categories")
        .continueWithTask {
            val categories = it.result!!.getAllBase()
            val categoryResource = it.result!!
            val hobbyCards = mutableListOf<HobbyCard>()
            Log.d("abc", "LoadHobbies")
            val categoryResourceList = categories.map { category -> "hobbies/category-$category" }
            RemoteResourcesManager(context)
                .findMultilingualResources(*categoryResourceList.toTypedArray())
                .continueWith {
                    Log.d("abc", "${it.result!!.size}")
                    categories.forEachIndexed { index, category ->
                        hobbyCards.add(
                            index,
                            HobbyCard(categoryResource.toCurrentLanguage(category), it.result!![index].getAll().toTypedArray())
                        )
                    }
                }.continueWith {
                    hobbyCards.toList()
                }
        }
}

