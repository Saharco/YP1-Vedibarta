package com.technion.vedibarta.studentsMatching.impl

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.technion.vedibarta.POJOs.Student
import com.technion.vedibarta.studentsMatching.Matcher

const val STUDENTS_LIMIT = 10
const val TAG = "Matcher"

/**
 * The default implementation of [Matcher].
 *
 * The matched students will all be a perfect match, and if needed, the rest will be an ok match.
 * Perfect students will always be placed before ok students in the returned lists.
 * The algorithm will match no more than [STUDENTS_LIMIT] students.
 *
 * Any student which fits the [characteristics], [region] and [school] will be considered a perfect
 * match. If there aren't enough students who do fit the parameters (less than [STUDENTS_LIMIT]),
 * any other student who has all-but-one of the specified [characteristics], will be considered an
 * ok match.
 *
 * @param studentsCollection a reference to the students collection where matches will be searched.
 * @param characteristics the wanted set of characteristics, which the matched users must have.
 * @param region if not null, the region in which the matched users must live.
 * @param school if not null, the school in which the matched users must study.
 */
class MatcherImpl(private val studentsCollection: CollectionReference,
                  private val characteristics: Collection<String>, private val region: String? = null,
                  private val school: String? = null) : Matcher {

    override fun match(): Task<Set<Student>> {
        return getDocs().continueWith { task ->
            task.result!!.map { it.toObject(Student::class.java)!! }.toSet()
        }
    }

    private fun getDocs(): Task<Set<DocumentSnapshot>> {
        return matchWithGivenCharacteristics(STUDENTS_LIMIT, characteristics)
            .continueWithTask { task ->
                val docList = task.result!!.documents.toSet()

                if ((docList.size < STUDENTS_LIMIT) and (characteristics.size > 1)) {
                    tryWithOneLessCharacteristic(STUDENTS_LIMIT - docList.size)
                        .continueWith { docList.union(it.result!!) }
                } else {
                    Tasks.call { docList }
                }
            }
    }

    private fun tryWithOneLessCharacteristic(amount: Int): Task<Set<DocumentSnapshot>> {
        val results = mutableListOf<DocumentSnapshot>()

        val characteristicsList = characteristics.toList()

        return Tasks.whenAll(characteristicsList.indices.map { i ->
            val newCharacteristics = characteristicsList.toMutableList()
            newCharacteristics.removeAt(i)

            matchWithGivenCharacteristics(
                amount,
                newCharacteristics,
                listOf(characteristicsList[i])
            ).continueWith { results.addAll(it.result!!.documents) }
        }).continueWith {
            results.shuffled().take(amount).toSet()
        }
    }

    private fun matchWithGivenCharacteristics(
        amount: Int,
        characteristics: Iterable<String>,
        disallowedCharacteristics: Iterable<String> = emptyList()
    ): Task<QuerySnapshot> {
        assert(characteristics.intersect(disallowedCharacteristics).isEmpty()) {
            "characteristics and disallowedCharacteristics shouldn't share any members"
        }

        var query: Query = studentsCollection

        if (region != null) {
            query = query.whereEqualTo("region", region)
        }

        if (school != null) {
            query = query.whereEqualTo("school", school)
        }

        for (characteristic in characteristics) {
            query = query.whereEqualTo("characteristics.$characteristic", true)
        }
        for (characteristic in disallowedCharacteristics) {
            query = query.whereEqualTo("characteristics.$characteristic", false)
        }

        return query.limit(amount.toLong()).get()
    }
}
