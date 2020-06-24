package com.technion.vedibarta.chatRoom

import com.technion.vedibarta.POJOs.Message
import com.technion.vedibarta.utilities.VedibartaActivity.Companion.database
import java.text.SimpleDateFormat
import java.util.*

/***
 * provides the ability to send messages (user/system) in a specific chat
 * errorCallback parameter is for propagating errors from this class to an outer context (aimed at activities)
 */
class MessageSender(
    private val adapter: ChatRoomAdapter,
    private val chatId: String,
    private val userId: String,
    private val partnerId: String,
    private val systemSenderId: String,
    private val errorCallback: (e: Exception) -> Unit)
{
    private val dateFormatter = SimpleDateFormat("dd/MM/yy", Locale.getDefault())

    /**
     * sends user message and optionally a message with the current date
     */
    fun sendMessage(text: String, isSystemMessage: Boolean)
    {
        sendDateMessageIfNeeded()
        write(text, isSystemMessage)
    }

    /**
     * adds a message document to firebase
     */
    private fun write(text: String, isSystemMessage: Boolean)
    {
        val sender = if (isSystemMessage) systemSenderId else userId
        database
            .chats()
            .chat(chatId)
            .messages()
            .build()
            .add(Message(sender, partnerId, text))
            .addOnFailureListener { errorCallback(it) }
    }

    /**
     * sends a message with the current date if more then a day had passed since the last message
     * or if there has been no messages yet
     */
    private fun sendDateMessageIfNeeded()
    {
        val lastMessage = adapter.getFirstMessageOrNull()
        if (lastMessage == null)
            write(dateFormatter.format(database.clock.getCurrentDate()), true)
        else
        {
            val lastMessageDate = lastMessage.timestamp ?: return
            if (database.clock.hasMoreThenADayPassed(lastMessageDate))
                write(dateFormatter.format(database.clock.getCurrentDate()), true)
        }
    }

}