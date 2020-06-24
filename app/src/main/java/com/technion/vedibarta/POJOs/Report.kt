package com.technion.vedibarta.POJOs

import android.os.Parcelable
import com.google.firebase.firestore.ServerTimestamp
import com.technion.vedibarta.utilities.VedibartaActivity.Companion.database
import kotlinx.android.parcel.Parcelize
import java.io.Serializable
import java.util.*

sealed class Report(
        open val senderId: String,
        open val receiverId: String,
        open val content: String,
        @ServerTimestamp
        val time: Date? = null
) : Serializable, Parcelable

@Parcelize
data class AbuseReport(
        override val senderId: String = "",
        val senderName: String = "",
        val senderPhoto: String? = null,
        val senderGender: Gender = Gender.MALE,
        val abuserId: String = "",
        val abuserName: String = "",
        val abuserPhoto: String? = null,
        val abuserGender: Gender = Gender.MALE,
        override val receiverId: String = "",
        override val content: String = ""
) : Report(senderId, receiverId, content), Serializable, Parcelable
