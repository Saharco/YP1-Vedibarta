package com.technion.vedibarta.ExtentionFunctions

import com.technion.vedibarta.POJOs.ChatCard
import com.technion.vedibarta.POJOs.Student
import com.technion.vedibarta.utilities.VedibartaActivity.Companion.student

fun ChatCard.create(other: Student): ChatCard {
    var s1: Student = student!!
    var s2: Student = other
    if (s1.uid > s2.uid) {
        s1 = other
        s2 = student!!
    }
    this.participantsId = listOf(s1.uid, s2.uid)
    this.participantsName = listOf(s1.name, s2.name)
    this.participantsPhoto = listOf(s1.photo, s2.photo)
    this.chat = "${participantsId[0]}${participantsId[1]}"

    return this
}

fun ChatCard.getPartnerId(myUserId: String): String {
    if (participantsId[0] != myUserId)
        return participantsId[0]
    return participantsId[1]
}

fun ChatCard.getName(userID: String): String {
    if (participantsId[0] == userID)
        return this.participantsName[0]
    return participantsName[1]
}

fun ChatCard.getPhoto(userID: String): String? {
    if (participantsId[0] == userID)
        return this.participantsPhoto[0]
    return participantsPhoto[1]
}