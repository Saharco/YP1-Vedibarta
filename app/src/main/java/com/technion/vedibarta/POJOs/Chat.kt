package com.technion.vedibarta.POJOs

import com.google.firebase.firestore.ServerTimestamp
import com.technion.vedibarta.data.TeacherMeta.Companion.teacher
import com.technion.vedibarta.utilities.VedibartaActivity
import com.technion.vedibarta.utilities.VedibartaActivity.Companion.userType
import java.io.Serializable
import java.util.*

data class Chat(
    var participantsName: List<String> = emptyList(),
    var participantsId: List<String> = emptyList(),
    var lastMessage: String = "",
    @ServerTimestamp
    var lastMessageTimestamp: Date? = null,
    val numMessages: Int = 0,
    var chat: String? = null
) : Serializable
{
    fun create(other: User): Chat
    {
        var u1: User =
                when (userType)
                {
                    UserType.Student -> VedibartaActivity.student!!
                    UserType.Teacher -> teacher
                }
        var u2: User = other
        if (u1.uid > u2.uid)
        {
            val temp = u1
            u1 = u2
            u2 = temp
        }
        this.participantsId = listOf(u1.uid, u2.uid)
        this.participantsName = listOf(u1.name, u2.name)
        this.chat = "${participantsId[0]}${participantsId[1]}"

        return this
    }

    fun getPartnerId(myUserId: String): String
    {
        if (participantsId[0] != myUserId)
            return participantsId[0]
        return participantsId[1]
    }

    fun getName(userID: String): String
    {
        if (participantsId[0] == userID)
            return this.participantsName[0]
        return participantsName[1]
    }

    override fun equals(other: Any?): Boolean
    {
        if (other is Chat)
            return (this.chat == other.chat) and (this.chat != null)
        return false
    }
}

