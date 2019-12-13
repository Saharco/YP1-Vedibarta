package com.technion.vedibarta.studentsMatching.impl

import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
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
    override fun match(): List<Student> {
        return getDocs().map { it.toObject(Student::class.java)!! }
    }

    override fun getDocs(): List<DocumentSnapshot> {
        val result = matchWithGivenCharacteristics(characteristics, STUDENTS_LIMIT.toLong()).toMutableList()

        if ((result.size < STUDENTS_LIMIT) and (characteristics.size > 1)) {
            result.addAll(tryWithOneLessCharacteristic(STUDENTS_LIMIT.toLong() - result.size))
        }

        return result
    }

    private fun tryWithOneLessCharacteristic(amount: Long): List<DocumentSnapshot> {
        val result = emptyList<DocumentSnapshot>().toMutableList()

        val shuffleCharacteristics = characteristics.shuffled()

        for (i in 0.. shuffleCharacteristics.size) {
            val newCharacteristics = shuffleCharacteristics.drop(i)
            result.addAll(matchWithGivenCharacteristics(newCharacteristics, amount - result.size))
        }

        return result
    }

    private fun matchWithGivenCharacteristics(characteristics: Iterable<String>, amount: Long): List<DocumentSnapshot> {
        var query = studentsCollection.whereEqualTo("region", region)
            .whereEqualTo("school", school)

        for (characteristic in characteristics) {
            query = query.whereEqualTo("characteristics.$characteristic", true)
        }

        return query.limit(amount).get()
            .addOnFailureListener { exp ->
                Log.w(TAG, "Error getting students documents", exp)
                throw exp
            }.result!!.documents
    }
}