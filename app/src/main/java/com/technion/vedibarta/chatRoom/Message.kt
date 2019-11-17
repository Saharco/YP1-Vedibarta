package com.technion.vedibarta.chatRoom

import java.text.SimpleDateFormat
import java.util.*

class Message
{
    var userId: Int = 0
    var text: String? = null
    private val fullTimeStamp = Date(System.currentTimeMillis())
    private val formatter = SimpleDateFormat("HH:mma")
    var timestamp = formatter.format(fullTimeStamp)

    constructor() //empty for firebase

    constructor(messageText: String)
    {
        text = messageText
    }
}