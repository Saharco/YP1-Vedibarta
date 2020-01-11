package com.technion.vedibarta.POJOs

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class Message(
    val sender: String = "",
    val receiver: String = "",
    var text: String = "",
    @ServerTimestamp
    var timestamp: Date? = null
)