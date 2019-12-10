package com.technion.vedibarta.POJOs

import java.util.*

data class ChatCard (
    val userPhotoId: Int = 0,
    val userName: String = "",
    var lastMessage: String = "",
    val date: String = "")
