package com.technion.vedibarta.POJOs

import java.io.Serializable
import java.util.*

data class ChatMetadata(
    val chatId: String, val partnerId: String, val partnerName: String,
    val numMessages: Int, val lastMessage: String, val lastMessageTimestamp: Date,
    val partnerGender: Gender = Gender.MALE, val partnerPhotoUrl: String? = null
) : Serializable