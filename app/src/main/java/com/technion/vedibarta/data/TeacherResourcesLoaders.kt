package com.technion.vedibarta.data

import android.content.Context
import com.google.android.gms.tasks.Task
import com.technion.vedibarta.POJOs.Bubble
import com.technion.vedibarta.utilities.resourcesManagement.MultilingualTextResource
import com.technion.vedibarta.utilities.resourcesManagement.RemoteTextResourcesManager

data class BubblesWithTranslator(
    val bubbles: List<Bubble>,
    val translator: MultilingualTextResource
)

private fun getBubblesResource(
    context: Context,
    resourceName: String
): Task<BubblesWithTranslator> {
    val textManager = RemoteTextResourcesManager(context)

    val task = textManager.findMultilingualResource(resourceName)

    return task.continueWith {
        val characteristics = task.result!!

        BubblesWithTranslator(
            characteristics.getAllBase().map {
                Bubble(it)
            },
            characteristics
        )
    }
}

fun loadSchoolCharacterstics(context: Context) =
    getBubblesResource(context,"school_characteristics")

fun loadSubjects(context: Context) =
    getBubblesResource(context,"subjects")
