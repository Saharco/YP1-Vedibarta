package com.technion.vedibarta.POJOs

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Parcelize
data class Class(
    val name: String = "",
    @DocumentId
    val id: String = "",
    val studentsIDs: List<String> = emptyList(),
    val teacherID: String = ""
): Parcelable, Serializable
