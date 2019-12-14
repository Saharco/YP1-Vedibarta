package com.technion.vedibarta.POJOs

import android.annotation.SuppressLint
import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import com.technion.vedibarta.utilities.Gender
import kotlinx.android.parcel.Parcelize
import java.io.Serializable
import java.util.*
import kotlin.collections.HashMap

@SuppressLint("ParcelCreator")
@Parcelize
data class Student(
    var name: String = "",
    var photo: String? = null,
    var region: String = "",
    var school: String = "",
    var gender: Gender = Gender.NONE,
    var lastActivity: Date? = null,
    var characteristics: MutableMap<String, Boolean> = HashMap(),
    var hobbies: List<String> = emptyList(),
    @DocumentId var documentId: String? = null
): Serializable, Parcelable