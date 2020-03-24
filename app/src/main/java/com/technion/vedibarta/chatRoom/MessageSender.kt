package com.technion.vedibarta.chatRoom

import android.util.Log
import com.technion.vedibarta.POJOs.Message
import com.technion.vedibarta.utilities.DocumentsCollections
import java.text.SimpleDateFormat
import java.util.*

/***
 * provides the ability to send messages (user/system) in a specific chat
 * errorCallback parameter is for propagating errors from this class to an outer context (aimed at activities)
 */
class MessageSender(
    private val adapter: ChatRoomAdapter,
    private val database: DocumentsCollections,
    private val chatId: String,
    private val userId: String,
    private val partnerId: String,
    private val systemSenderId: String,
    private val errorCallback: (e: Exception) -> Unit)
{
    private val dateFormatter = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
    private val reversedDateFormatter = SimpleDateFormat("yy/MM/dd", Locale.getDefault())

    fun sendMessage(text: String, isSystemMessage: Boolean)
    {
        sendDateMessageIfNeeded()
        write(text, isSystemMessage)
    }

    private fun write(text: String, isSystemMessage: Boolean)
    {
        val sender = if (isSystemMessage) systemSenderId else userId
        database
            .chats()
            .chatId(chatId)
            .messages()
            .build()
            .add(Message(sender, partnerId, text))
            .addOnFailureListener { errorCallback(it) }
    }

    private fun sendDateMessageIfNeeded(currentDate: Date = Date())
    {
        if (adapter.hasNoMessages() or hasMoreThenADayPassed(currentDate))
            write(dateFormatter.format(currentDate), true)
    }

    private fun hasMoreThenADayPassed(currentDate: Date): Boolean
    {
        val lastMessageDate = adapter.getFirstMessageOrNull()?.timestamp ?: Date()
        return reversedDateFormatter.format(currentDate) > reversedDateFormatter.format(lastMessageDate)
    }
}