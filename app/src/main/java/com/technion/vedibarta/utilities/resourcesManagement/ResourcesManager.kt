package com.technion.vedibarta.utilities.resourcesManagement

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.technion.vedibarta.POJOs.Gender

/**
 * An interface for getting and managing text resources.
 *
 * Use it to get resources based on app's preferences.
 */
interface ResourcesManager {
    /**
     * @param name the name of the resource to find
     * @param gender override the app's gender preference or null to disable override.
     * @return a [Task] containing the resource
     */
    fun findResource(name: String, gender: Gender? = null): Task<out Resource>

    /**
     * @param name the name of the resource to find
     * @param gender override the app's gender preference or null to disable override.
     * @return a [Task] containing the resource
     */
    fun findMultilingualResource(name: String, gender: Gender? = null): Task<out MultilingualResource>
}

fun ResourcesManager.findResources(vararg names: String, gender: Gender? = null):
        Task<List<Resource>> {
    val resourcesMap = mutableMapOf<String, Resource>()
    val tasks = names.map { name ->
        findResource(name, gender).continueWith { resourcesMap[name] = it.result!! }
    }

    return Tasks.whenAll(tasks).continueWith {
        names.map { resourcesMap[it]!! }
    }
}

fun ResourcesManager.findMultilingualResources(vararg names: String, gender: Gender? = null):
        Task<List<MultilingualResource>> {
    val resourcesMap = mutableMapOf<String, MultilingualResource>()
    Log.d("abc", "$names")
    val tasks = names.map { name ->
        findMultilingualResource(name, gender).continueWith { resourcesMap[name] = it.result!! }
    }
    Log.d("abc", "Called1")
    return Tasks.whenAll(tasks).continueWith {
        Log.d("abc", "Called2")
        names.map { resourcesMap[it]!! }
    }
}
