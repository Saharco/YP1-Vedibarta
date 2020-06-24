package com.technion.vedibarta.matching

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.technion.vedibarta.matching.impl.DocumentsMatcherImpl

/**
 * An interface for a matching algorithm of documents in the Firestore database.
 *
 * This interface allows to create more complex queries which are not directly supported through the
 * Firestore API.
 *
 * You can create a new [DocumentsMatcher] by using either [ofCollection] or [fromQuery].
 *
 * NOTICE: DO NOT CHAIN MORE THAT A FEW CALLS OF [whereAtLeastOneFieldMatch] OR [whereAtMostOneFieldNotMatch].
 *  The time, memory and queries invocations amount grows exponentially with the amount of calls.
 */
interface DocumentsMatcher {
    companion object {

        /**
         * Returns a new [DocumentsMatcher] that searches matching documents in [collection].
         */
        fun ofCollection(collection: CollectionReference): DocumentsMatcher =
            DocumentsMatcherImpl(collection)

        /**
         * Returns a new [DocumentsMatcher] that searches for matches only in the set of documents
         * that satisfies [query].
         */
        fun fromQuery(query: Query): DocumentsMatcher =
            DocumentsMatcherImpl(query)
    }

    /**
     * Applies [Query.whereEqualTo] with all fields and values specified in [fields].
     */
    fun whereFieldsMatch(fields: Map<String, Any>): DocumentsMatcher

    /**
     * At least one of the [fields] must be equal to the specified value in the matched documents.
     *
     * NOTICE: calling [match] on the returned object will multiply the amount of queries invocation
     *  by the size of [fields].
     */
    fun whereAtLeastOneFieldMatch(fields: Map<String, Any>): DocumentsMatcher

    /**
     * All [fields] must match their appropriate values in the matched documents, but with one
     * degree on freedom.
     *
     * NOTICE: calling [match] on the returned object will multiply the amount of queries invocation
     *  by the size of [fields] + 1.
     */
    fun whereAtMostOneFieldNotMatch(fields: Map<String, Any>): DocumentsMatcher

    /**
     * Applies [Query.whereArrayContains] with the given field.
     */
    fun whereArrayFieldContains(field: Pair<String, Any>): DocumentsMatcher

    /**
     * Applies [Query.whereEqualTo] with the given [field] for each value in [values].
     */
    fun whereFiledEqualsToOneOf(field: String, values: Iterable<Any>): DocumentsMatcher

    /**
     * Applies all the queries that were aggregated during the creation of the object.
     *
     * @param randomField if not null, randomly cycle using [randomField]
     * @param limit the max amount of documents to return
     */
    fun match(randomField: String? = null, limit: Long = 10): Task<List<DocumentSnapshot>>
}

fun DocumentsMatcher.whereFieldsMatch(vararg fields: Pair<String, String>) =
    whereFieldsMatch(fields.toMap())
