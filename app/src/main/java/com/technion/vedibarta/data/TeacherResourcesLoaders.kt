package com.technion.vedibarta.data

import android.content.Context
import com.google.android.gms.tasks.Task
import com.technion.vedibarta.POJOs.Bubble
import com.technion.vedibarta.POJOs.CategoryCard
import com.technion.vedibarta.R
import com.technion.vedibarta.utilities.resourcesManagement.MultilingualTextResource
import com.technion.vedibarta.utilities.resourcesManagement.RemoteTextResourcesManager

data class CardWithTranslator(
    val card: CategoryCard,
    val translator: MultilingualTextResource
)

private fun getBubblesResource(
    context: Context,
    resourceName: String
): Task<CardWithTranslator> {
    val textManager = RemoteTextResourcesManager(context)

    val task = textManager.findMultilingualResource(resourceName)

    return task.continueWith {
        val characteristics = task.result!!

        CardWithTranslator(
            CategoryCard(
                "title",
                characteristics.getAllBase().map { Bubble(it) },
                showBackgrounds = false,
                isToggleable = false
            ),
            characteristics
        )
    }
}

fun loadSchoolCharacteristics(context: Context) = getBubblesResource(
    context,
    "school_characteristics"
)

fun loadSubjects(context: Context) = getBubblesResource(
    context,
    "subjects"
)
