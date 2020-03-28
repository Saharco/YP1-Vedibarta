package com.technion.vedibarta.utilities.resourcesManagement

import com.google.android.gms.tasks.Task

/**
 * An interface for getting and managing text resources.
 *
 * Use it to get resources based on app's preferences.
 */
interface ResourcesManager {
    /**
     * @param name the name of the resource to find
     * @return a [Task] containing the resource
     */
    fun findResource(name: String): Task<out Resource>

    /**
     * @param name the name of the resource to find
     * @return a [Task] containing the resource
     */
    fun findMultilingualResource(name: String): Task<out MultilingualResource>
}
