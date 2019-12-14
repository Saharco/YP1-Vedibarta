package com.technion.vedibarta.POJOs

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import com.technion.vedibarta.utilities.Gender
import kotlinx.android.parcel.Parcelize
import java.io.Serializable
import java.util.*
import kotlin.collections.HashMap

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
    val uid: String = ""
): Serializable