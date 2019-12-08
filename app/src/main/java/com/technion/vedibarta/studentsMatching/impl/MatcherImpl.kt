package com.technion.vedibarta.studentsMatching.impl

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.technion.vedibarta.POJOs.Student
import com.technion.vedibarta.studentsMatching.Matcher

const val STUDENTS_LIMIT = 10
const val TAG = "Matcher"

class MatcherImpl(private val characteristics: List<String>, private val region: String? = null, private val school: String? = null) : Matcher {
    private val studentsCollection = FirebaseFirestore.getInstance().collection("students")

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

    private fun matchWithGivenCharacteristics(characteristics: List<String>, amount: Long): List<DocumentSnapshot> {
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