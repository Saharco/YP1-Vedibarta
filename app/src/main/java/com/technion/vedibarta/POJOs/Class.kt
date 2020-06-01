package com.technion.vedibarta.POJOs

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Parcelize
data class Class(
    var name: String = "",
    @DocumentId
    val id: String = "",
    var studentsIDs: List<String> = emptyList(),
    val teacherID: String = "",
    var description: String = "",
    var photo: String? = null
): Parcelable, Serializable
