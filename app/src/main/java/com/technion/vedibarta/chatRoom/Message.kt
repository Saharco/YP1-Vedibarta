package com.technion.vedibarta.chatRoom

import java.util.*

class Message
{
    var userId: Int = 0
    var text: String? = null
    var timestamp = Date(System.currentTimeMillis()).toString()

    constructor() //empty for firebase

    constructor(messageText: String)
    {
        text = messageText
    }
}