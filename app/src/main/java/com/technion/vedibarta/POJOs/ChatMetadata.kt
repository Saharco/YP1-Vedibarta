package com.technion.vedibarta.POJOs

import java.io.Serializable
import java.util.*

data class ChatMetadata(val chatId: String = "",
                        val partnerId: String = "",
                        val partnerName: String = "",
                        val numMessages: Int = 0,
                        val lastMessage: String = "",
                        val lastMessageTimestamp: Date = Date(),
                        val partnerGender: Gender = Gender.MALE,
                        val partnerPhotoUrl: String? = null,
                        val partnerHobbies: Array<String> = emptyArray()) : Serializable
{
    companion object
    {
        fun create(chat: Chat, userId: String, partner: Student?): ChatMetadata
        {
            val partnerId = chat.getPartnerId(userId)
            if (partner == null)
            {
                return ChatMetadata(chat.chat!!,
                                    partnerId,
                                    chat.getName(partnerId),
                                    chat.numMessages,
                                    chat.lastMessage,
                                    chat.lastMessageTimestamp)
            }
            else
            {
                return ChatMetadata(chat.chat!!,
                                    partnerId,
                                    chat.getName(partnerId),
                                    chat.numMessages,
                                    chat.lastMessage,
                                    chat.lastMessageTimestamp,
                                    partner.gender,
                                    partner.photo,
                                    partner.hobbies.toTypedArray())
            }
        }
    }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (other !is ChatMetadata) return false

        return (chatId == other.chatId) and (partnerId == other.partnerId)

    }

    override fun hashCode(): Int
    {
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