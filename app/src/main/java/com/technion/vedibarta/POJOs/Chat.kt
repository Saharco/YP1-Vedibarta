package com.technion.vedibarta.POJOs

import java.io.Serializable
import java.util.*

data class Chat (
    var participantsName: List<String> = emptyList(),
    var participantsId: List<String> = emptyList(),
    var lastMessage: String = "",
    var lastMessageTimestamp: Date = Date(System.currentTimeMillis()),
    val numMessages: Int = 0,
    var chat: String? = null
): Serializable

