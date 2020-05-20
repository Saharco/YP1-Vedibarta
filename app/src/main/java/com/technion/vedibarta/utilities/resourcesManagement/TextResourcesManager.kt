package com.technion.vedibarta.utilities.resourcesManagement

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.technion.vedibarta.POJOs.Gender

/**
 * An interface for getting and managing text resources.
 *
 * Use it to get resources based on app's preferences.
 */
interface TextResourcesManager {
    /**
     * @param name the name of the resource to find
     * @param gender override the app's gender preference or null to disable override.
     * @return a [Task] containing the resource
     */
    fun findResource(name: String, gender: Gender? = null): Task<out TextResource>

    /**
     * @param name the name of the resource to find
     * @param gender override the app's gender preference or null to disable override.
     * @return a [Task] containing the resource
     */
    fun findMultilingualResource(name: String, gender: Gender? = null): Task<out MultilingualTextResource>
}

fun TextResourcesManager.findResources(vararg names: String, gender: Gender? = null):
        Task<List<TextResource>> {
    val resourcesMap = mutableMapOf<String, TextResource>()
    val tasks = names.map { name ->
        findResource(name, gender).continueWith { resourcesMap[name] = it.result!! }
    }

    return Tasks.whenAll(tasks).continueWith {
        names.map { resourcesMap[it]!! }
    }
}

fun TextResourcesManager.findMultilingualResources(vararg names: String, gender: Gender? = null):
        Task<List<MultilingualTextResource>> {
    if (names.isEmpty()) return Tasks.call { emptyList<MultilingualTextResource>() }
    val resourcesMap = mutableMapOf<String, MultilingualTextResource>()
    val tasks = names.map { name ->
        findMultilingualResource(name, gender).continueWith { resourcesMap[name] = it.result!! }
    }
    return Tasks.whenAll(tasks).continueWith {
        names.map { resourcesMap[it]!! }
    }
}
