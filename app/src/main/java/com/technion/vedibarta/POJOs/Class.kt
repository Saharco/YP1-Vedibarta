package com.technion.vedibarta.POJOs

import com.google.firebase.firestore.DocumentId
import java.io.Serializable

/***
 * [Class] represents a class document in database
 * [id] is automatically populated by FireBase when document is read
 */
data class Class(val name: String = "",
                 val students: MutableList<StudentParticipant> = mutableListOf()): Serializable
{
    @DocumentId val id: String = ""

    data class StudentParticipant(val id: String = "", var wasReported: Boolean = false): Serializable
}
