package com.technion.vedibarta.ExtentionFunctions

import com.technion.vedibarta.POJOs.Chat
import com.technion.vedibarta.POJOs.Student
import com.technion.vedibarta.utilities.VedibartaActivity.Companion.student

fun Chat.create(other: Student): Chat {
    var s1: Student = student!!
    var s2: Student = other
    if (s1.uid > s2.uid) {
        s1 = other
        s2 = student!!
    }
    this.participantsId = listOf(s1.uid, s2.uid)
    this.participantsName = listOf(s1.name, s2.name)
    this.chat = "${participantsId[0]}${participantsId[1]}"

    return this
}

fun Chat.getPartnerId(myUserId: String): String {
    if (participantsId[0] != myUserId)
        return participantsId[0]
    return participantsId[1]
}

fun Chat.getName(userID: String): String {
    if (participantsId[0] == userID)
        return this.participantsName[0]
    return participantsName[1]
}