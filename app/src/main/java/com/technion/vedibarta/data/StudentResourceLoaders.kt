package com.technion.vedibarta.data

import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.technion.vedibarta.POJOs.Bubble
import com.technion.vedibarta.POJOs.Gender
import com.technion.vedibarta.POJOs.CategoryCard
import com.technion.vedibarta.utilities.resourcesManagement.*

fun loadCharacteristicsTranslator(
    context: Context,
    gender: Gender
): Task<MultilingualTextResource> {
    val textManager = RemoteTextResourcesManager(context)

    val allTask = textManager.findMultilingualResource("characteristics/all", gender)
    val categoriesTask = textManager.findMultilingualResource("characteristics/categories")

    return Tasks.whenAllSuccess<MultilingualTextResource>(
        allTask,
        categoriesTask
    ).continueWith {
        merge(it.result!!)
    }
}

fun loadCharacteristicsCards(
    context: Context
): Task<List<CategoryCard>> {
    val textManager = RemoteTextResourcesManager(context)

    val categoriesTask = textManager.findMultilingualResource("characteristics/categories")

    return categoriesTask.continueWithTask {
        val categoriesResource = categoriesTask.result!!
        val categories = categoriesResource.getAllBase()

        Tasks.whenAllSuccess<CategoryCard>(categories.map { category ->
            textManager.findResource("characteristics/category-$category").continueWith { categoryResourceTask ->
                val categoryResource = categoryResourceTask.result!!

                CategoryCard(
                    category,
                    categoryResource.getAll().map { Bubble(it) },
                    showBackgrounds = false,
                    isToggleable = true
                )
            }
        })
    }
}

data class CardsWithTranslator(
    val cards: List<CategoryCard>,
    val translator: MultilingualTextResource
)

fun loadCharacteristicsCardsWithTranslator(
    context: Context,
    gender: Gender
): Task<CardsWithTranslator> {
    val textManager = RemoteTextResourcesManager(context)

    val allTask = textManager.findMultilingualResource("characteristics/all", gender)
    val categoriesTask = textManager.findMultilingualResource("characteristics/categories")

    val cardsTask = categoriesTask.continueWithTask<List<CategoryCard>> {
        val categoriesResource = categoriesTask.result!!
        val categories = categoriesResource.getAllBase()

        Tasks.whenAllSuccess(categories.map { category ->
            textManager.findResource("characteristics/category-$category").continueWith { categoryResourceTask ->
                val categoryResource = categoryResourceTask.result!!

                CategoryCard(
                    category,
                    categoryResource.getAll().map { Bubble(it) },
                    showBackgrounds = false,
                    isToggleable = true
                )
            }
        })
    }

    return Tasks.whenAll(allTask, cardsTask).continueWith {
        CardsWithTranslator(cardsTask.result!!, merge(allTask.result!!, categoriesTask.result!!))
    }
}

fun loadHobbiesCardsWithTranslator(
    context: Context
): Task<CardsWithTranslator> {
    val textManager = RemoteTextResourcesManager(context)
    val fileManager = RemoteFileResourcesManager(context)

    val allTask = textManager.findMultilingualResource("hobbies/all")
    val categoriesTask = textManager.findMultilingualResource("hobbies/categories")
    val imagesTask = fileManager.getAllInDirectory("images/hobbies")

    val categoryResourcesTask = categoriesTask.continueWithTask<List<Pair<String, TextResource>>> {
        val categoriesResource = categoriesTask.result!!
        val categories = categoriesResource.getAllBase()

        Tasks.whenAllSuccess(categories.map { category ->
            textManager.findResource("hobbies/category-$category").continueWith {
                Pair(category, it.result!!)
            }
        })
    }

    val cardsTask = Tasks.whenAll(categoryResourcesTask, imagesTask).continueWith {
        val imagesMap = imagesTask.result!!
        val categoryResources = categoryResourcesTask.result!!

        categoryResources.map { (category, resource) ->
            CategoryCard(
                category,
                resource.getAll().map {
                    Bubble(it, imagesMap["$it.jpg"])
                },
                showBackgrounds = true,
                isToggleable = false
            )
        }
    }

    return Tasks.whenAll(allTask, cardsTask).continueWith {
        CardsWithTranslator(cardsTask.result!!, merge(allTask.result!!, categoriesTask.result!!))
    }
}