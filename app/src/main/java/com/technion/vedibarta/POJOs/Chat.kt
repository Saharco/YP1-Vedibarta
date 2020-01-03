package com.technion.vedibarta.POJOs

import com.technion.vedibarta.utilities.VedibartaActivity
import java.io.Serializable
import java.util.*

data class Chat (
    var participantsName: List<String> = emptyList(),
    var participantsId: List<String> = emptyList(),
    var lastMessage: String = "",
    var lastMessageTimestamp: Date = Date(System.currentTimeMillis()),
    val numMessages: Int = 0,
    var chat: String? = null
): Serializable
{
    fun create(other: Student): Chat {
        var s1: Student = VedibartaActivity.student!!
        var s2: Student = other
        if (s1.uid > s2.uid) {
            s1 = other
            s2 = VedibartaActivity.student!!
        }
        this.participantsId = listOf(s1.uid, s2.uid)
        this.participantsName = listOf(s1.name, s2.name)
        this.chat = "${participantsId[0]}${participantsId[1]}"

        return this
    }

    fun getPartnerId(myUserId: String): String {
        if (participantsId[0] != myUserId)
            return participantsId[0]
        return participantsId[1]
    }

    fun getName(userID: String): String {
        if (participantsId[0] == userID)
            return this.participantsName[0]
        return participantsName[1]
    }

    override fun equals(other: Any?): Boolean
    {
        if (other is Chat)
            return this.participantsId.containsAll(other.participantsId)
        return false
    }
}

