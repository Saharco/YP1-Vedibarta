package com.technion.vedibarta.utilities.resourcesManagement

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import java.lang.Exception

/**
 * A logical class for resolving resources by preferences.
 *
 * This class resolves the best matching string out of a given collection according to some given
 * preferences.
 *
 * @param preferences a [Map] from preference names to their respective values.
 */
class PreferencesResolver(private val preferences: Map<String, *>) {
    constructor(preferences: SharedPreferences) : this(preferences.all)
    /**
     * The used preferences will be the app's default shared preferences.
     */
    constructor(context: Context) : this(PreferenceManager.getDefaultSharedPreferences(context))

    /**
     * Chooses the best matching possibility out of [possibilities]. That is, the one that match
     * every preference in [preferences] (or doesn't specify some of them). If possibility A
     * specifies and matches every preference that possibility B specifies and matches but
     * possibility A specifies and matches more preferences, possibility A will be considered
     * better. If two possibilities specify and match the same exact preferences, the least
     * specific one will be considered better. If there are multiple possibilities where none is
     * better than the others one of them will be chosen - which one is undefined.
     *
     * Each possibility should be in the form "prefix-pref1=val1-pref2=val2-...".
     *
     * @returns the best match out of [possibilities].
     * @throws [NoMatchException] if there wasn't any match.
     */
    fun resolve(possibilities: Collection<String>): String {
        // An auxiliary recursive function. entries is a list of possibilities and their map forms.
        fun resolveAux(entries: List<Pair<String, Map<String, String>>>, keys: Set<String>): String {
            // If no preferences left, choose the least specific possibility.
            if (keys.isEmpty())
                return entries.sortedBy { it.second.size }.first().first

            // Choosing the current preference.
            val key = keys.first()
            val value = preferences[key]

            // Skip preferences that no valid possibility specifies.
            if (entries.none { it.second.containsKey(key) })
                return resolveAux(entries, keys - key)

            val entriesMatch = entries.filter { it.second[key] == value }
            if (entriesMatch.isNotEmpty())
                return resolveAux(entriesMatch, keys - key)

            // If none of the possibilities matches the current preference, continue with those that
            // did not specify it.
            val entriesUnspecified = entries.filter { !it.second.containsKey(key) }
            return if (entriesUnspecified.isNotEmpty())
                resolveAux(entriesUnspecified, keys - key)
            else throw NoMatchException("Expected $key = $value but no match was found")
        }

        if (possibilities.isEmpty())
            throw NoMatchException("Got an empty collection")

        val entries = possibilities.toList()
        val entriesMap = entries.toMaps()

        return resolveAux(entries.zip(entriesMap), preferences.keys)
    }

    // Transforms a string of preferences in the form "prefix-pref1=val1-pref2=val2-..." to a map
    // from preferences names to their values.
    private fun String.toMap(): Map<String, String> =
        split('-')
            .drop(1)
            .map { Pair(it.substringBefore('='), it.substringAfter('=')) }
            .toMap()

    private fun List<String>.toMaps() = map { it.toMap() }

    class NoMatchException(message: String): Exception(message)
}
