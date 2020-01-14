package com.technion.vedibarta.POJOs

import java.io.Serializable
import java.util.*

data class ChatMetadata(
    val chatId: String, val partnerId: String, val partnerName: String,
    val numMessages: Int, val lastMessage: String, val lastMessageTimestamp: Date,
    val partnerGender: Gender = Gender.MALE, val partnerPhotoUrl: String? = null,
    val partnerHobbies : Array<String> = emptyArray()
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChatMetadata

        if (chatId != other.chatId) return false
        if (partnerId != other.partnerId) return false
        if (partnerName != other.partnerName) return false
        if (numMessages != other.numMessages) return false
        if (lastMessage != other.lastMessage) return false
        if (lastMessageTimestamp != other.lastMessageTimestamp) return false
        if (partnerGender != other.partnerGender) return false
        if (partnerPhotoUrl != other.partnerPhotoUrl) return false
        if (!partnerHobbies.contentEquals(other.partnerHobbies)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = chatId.hashCode()
        result = 31 * result + partnerId.hashCode()
        result = 31 * result + partnerName.hashCode()
        result = 31 * result + numMessages
        result = 31 * result + lastMessage.hashCode()
        result = 31 * result + lastMessageTimestamp.hashCode()
        result = 31 * result + partnerGender.hashCode()
        result = 31 * result + (partnerPhotoUrl?.hashCode() ?: 0)
        result = 31 * result + partnerHobbies.contentHashCode()
        return result
    }
}