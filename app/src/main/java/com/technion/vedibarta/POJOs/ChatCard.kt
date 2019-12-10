package com.technion.vedibarta.POJOs

import java.util.*

data class ChatCard (
    val userPhoto: String = "",
    val userName: String = "",
    val userId: String = "",
    var lastMessage: String = "",
    val relativeTime: String = "",
    val numMessages: Int = 0,
    val date: String = "")
