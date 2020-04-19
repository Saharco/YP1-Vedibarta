package com.technion.vedibarta.utilities.extensions

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
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
fun Query.randomCycling(field: String, limit: Long): Task<List<DocumentSnapshot>> =
    whereGreaterThanOrEqualTo(field, randomId()).orderBy(field).limit(limit).getAsList()
        .continueWithTask { task1 ->
            val docList = task1.result!!
            if (docList.size < limit) {
                orderBy(field).limit(limit - docList.size).get()
                    .continueWith { task2 ->
                        docList.apply { addAll(task2.result!!.documents) }.distinct()
                    }
            } else {
                Tasks.call { docList }
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

fun Query.getAsList() = get().continueWith { it.result!!.documents }
