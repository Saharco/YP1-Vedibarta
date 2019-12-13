package com.technion.vedibarta.POJOs

import java.text.SimpleDateFormat
import java.util.*

data class Message(val sender: String = "",
                   var text: String = "",
                   val fullTimeStamp: Date = Date(System.currentTimeMillis()))
{
    fun getTime(): String
    {
        return SimpleDateFormat("HH:mma").format(fullTimeStamp)
    }
}