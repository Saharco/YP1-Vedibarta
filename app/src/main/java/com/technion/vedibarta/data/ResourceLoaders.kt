package com.technion.vedibarta.data

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Task
import com.technion.vedibarta.POJOs.Gender
import com.technion.vedibarta.POJOs.CategoryCard
import com.technion.vedibarta.utilities.resourcesManagement.RemoteTextResourcesManager
import com.technion.vedibarta.utilities.resourcesManagement.findMultilingualResources
import com.technion.vedibarta.utilities.resourcesManagement.toCurrentLanguage

typealias CategoriesMapper = Map<String, Array<String>>

fun loadCharacteristics(
    context: Context,
    gender: Gender
): Task<CategoriesMapper> {

    return RemoteTextResourcesManager(context)
        .findMultilingualResource("characteristics/categories")
        .continueWithTask {
            val categories = it.result!!.getAllBase()
            val categoryResource = it.result!!
            val characteristicsMap = mutableMapOf<String, Array<String>>()
            val categoryResourceList = categories.map { category -> "characteristics/category-$category" }
            RemoteTextResourcesManager(context)
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
): Task<List<CategoryCard>> {

    return RemoteTextResourcesManager(context)
        .findMultilingualResource("hobbies/categories")
        .continueWithTask {
            val categories = it.result!!.getAllBase()
            val categoryResource = it.result!!
            val hobbyCards = mutableListOf<CategoryCard>()
            Log.d("abc", "LoadHobbies")
            val categoryResourceList = categories.map { category -> "hobbies/category-$category" }
            RemoteTextResourcesManager(context)
                .findMultilingualResources(*categoryResourceList.toTypedArray())
                .continueWith {
                    Log.d("abc", "${it.result!!.size}")
                    categories.forEachIndexed { index, category ->
                        hobbyCards.add(
                            index,
                            CategoryCard(categoryResource.toCurrentLanguage(category), it.result!![index].getAll().toTypedArray())
                        )
                    }
                }.continueWith {
                    hobbyCards.toList()
                }
        }
}

