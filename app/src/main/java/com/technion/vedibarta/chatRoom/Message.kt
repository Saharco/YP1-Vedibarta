package com.technion.vedibarta.chatRoom

import java.text.SimpleDateFormat
import java.util.*

class Message
{
    var userId: Int = 0
    var text: String? = null
    private val formatter = SimpleDateFormat("HH:mma")
    var timestamp = formatter.format(Date(System.currentTimeMillis()))

    constructor() //empty for firebase

    constructor(messageText: String)
    {
        text = messageText
    }
}