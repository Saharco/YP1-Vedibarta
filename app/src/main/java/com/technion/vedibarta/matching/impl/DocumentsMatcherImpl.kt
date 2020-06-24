package com.technion.vedibarta.matching.impl

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.technion.vedibarta.matching.DocumentsMatcher
import com.technion.vedibarta.utilities.extensions.getAsList
import com.technion.vedibarta.utilities.extensions.randomCycling


class DocumentsMatcherImpl
constructor(private val query: Query) : DocumentsMatcher {

    override fun whereFieldsMatch(fields: Map<String, Any>): DocumentsMatcher = DocumentsMatcherImpl(
        fields.entries.fold(query) { query, (key, value) ->
            query.whereEqualTo(key, value)
        }
    )

    override fun whereAtLeastOneFieldMatch(fields: Map<String, Any>): DocumentsMatcher = DocumentsMatchersMerger(
        listOf(whereFieldsMatch(fields)) +
                fields.map { (key, _) ->
                    whereFieldsMatch(fields.toMutableMap().apply { remove(key) })
                }
    )

    override fun whereAtMostOneFieldNotMatch(fields: Map<String, Any>): DocumentsMatcher = DocumentsMatchersMerger(
        fields.map { (key, _) ->
            whereFieldsMatch(fields.toMutableMap().apply { remove(key) })
        }
    )

    override fun whereFiledEqualsToOneOf(field: String, values: Iterable<Any>): DocumentsMatcher = DocumentsMatchersMerger(
        values.map {
            DocumentsMatcherImpl(query.whereEqualTo(field, it))
        }.toList()
    )

    override fun whereArrayFieldContains(field: Pair<String, Any>): DocumentsMatcher = DocumentsMatcherImpl(
        query.whereArrayContains(field.first, field.second)
    )

    override fun match(randomField: String?, limit: Long): Task<List<DocumentSnapshot>> =
        if (randomField != null) {
            query.randomCycling(randomField, limit)
        } else {
            query.limit(limit).getAsList()
        }
}


private class DocumentsMatchersMerger(private val documentsMatchers: List<DocumentsMatcher>) :
    DocumentsMatcher {
    override fun whereFieldsMatch(fields: Map<String, Any>): DocumentsMatcher =
        DocumentsMatchersMerger(documentsMatchers.map { it.whereFieldsMatch(fields) })

    override fun whereAtLeastOneFieldMatch(fields: Map<String, Any>): DocumentsMatcher =
        DocumentsMatchersMerger(documentsMatchers.map { it.whereAtLeastOneFieldMatch(fields) })

    override fun whereAtMostOneFieldNotMatch(fields: Map<String, Any>): DocumentsMatcher =
        DocumentsMatchersMerger(documentsMatchers.map { it.whereAtMostOneFieldNotMatch(fields) })

    override fun whereArrayFieldContains(field: Pair<String, Any>): DocumentsMatcher =
        DocumentsMatchersMerger(documentsMatchers.map { it.whereArrayFieldContains(field) })

    override fun whereFiledEqualsToOneOf(field: String, values: Iterable<Any>): DocumentsMatcher =
        DocumentsMatchersMerger(documentsMatchers.map { it.whereFiledEqualsToOneOf(field, values) })

    override fun match(randomField: String?, limit: Long): Task<List<DocumentSnapshot>> =
        Tasks.whenAllSuccess<Set<DocumentSnapshot>>(documentsMatchers.map { it.match(randomField, limit) })
            .continueWith { it.result!!.flatten().distinct().take(limit.toInt()) }
}
