package com.technion.vedibarta.studentsMatching

import com.google.firebase.firestore.DocumentSnapshot
import com.technion.vedibarta.POJOs.Student

/**
 * An interface for a matching algorithm between students.
 */
interface Matcher {
    /**
     * Run the matching algorithm and return the matched students.
     * @return a [List] of [Student] objects, the matched students.
     */
    fun match(): List<Student>

    /**
     * Run the matching algorithm and return the documents of the matched students.
     * @return a [List] of [DocumentSnapshot], the matched students' documents at time of running.
     */
    fun getDocs(): List<DocumentSnapshot>
}