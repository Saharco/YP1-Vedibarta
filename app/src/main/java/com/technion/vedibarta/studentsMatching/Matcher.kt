package com.technion.vedibarta.studentsMatching

import com.google.firebase.firestore.DocumentSnapshot
import com.technion.vedibarta.POJOs.Student


interface Matcher {
    fun match(): List<Student>
    fun getDocs(): List<DocumentSnapshot>
}