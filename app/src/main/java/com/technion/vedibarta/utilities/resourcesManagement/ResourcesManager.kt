package com.technion.vedibarta.utilities.resourcesManagement

import com.google.android.gms.tasks.Task
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
