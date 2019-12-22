package com.technion.vedibarta.utilities.extensions

import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.Query
import kotlin.random.Random

/**
 * Randomizes the returned documents sequences in a cyclic manner.
 * The returned documents sequence are always consecutive (according to their ID).
 *
 * Note: the method uses the ascending indexing of [field].
 *
 * @param field the document's ID.
 * @param limit the maximal amount of documents to return.
 */
fun Query.randomCycling(field: String, limit: Long) =
    this.whereGreaterThanOrEqualTo(field, randomId()).orderBy(field).limit(limit).get()
        .continueWithTask { task1 ->
            val docSet = task1.result!!.documents.toSet()
            if (docSet.size < limit) {
                this.orderBy(field).limit(limit - task1.result!!.size()).get()
                    .continueWith { task2 ->
                        task1.result!!.documents.union(task2.result!!.documents)
                    }
            } else {
                Tasks.call { docSet }
            }
        }

private fun randomId(): String {
    val builder = StringBuilder()
    val maxRandom = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".length

    for (i in 0..19) {
        builder.append("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"[Random.nextInt(maxRandom)])
    }

    return builder.toString()
}
