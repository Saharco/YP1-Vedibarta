package com.technion.vedibarta.POJOs

import java.text.SimpleDateFormat
import java.util.*

data class Message(val messageType: MessageType = MessageType.USER,
                   var text: String = "",
                   val fullTimeStamp: Date = Date(System.currentTimeMillis()))
{
    fun getTime(): String
    {
        return SimpleDateFormat("HH:mma").format(fullTimeStamp)
    }
}

enum class MessageType
{
    USER,
    OTHER,
    GENERATOR
}