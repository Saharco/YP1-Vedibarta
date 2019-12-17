package com.technion.vedibarta.POJOs

import com.google.firebase.firestore.DocumentId
import java.io.Serializable
import java.util.*

data class ChatCard (
    var participantsPhoto: List<String?> = emptyList(),
    var participantsName: List<String> = emptyList(),
    var participantsId: List<String> = emptyList(),
    var lastMessage: String = "",
    var lastMessageTimestamp: Date = Date(System.currentTimeMillis()),
    val numMessages: Int = 0,
    var chat: String? = null
): Serializable

