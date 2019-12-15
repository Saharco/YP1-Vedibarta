package com.technion.vedibarta.ExtentionFunctions

import com.technion.vedibarta.POJOs.ChatCard
import com.technion.vedibarta.POJOs.Student

fun ChatCard.create(a: Student, b: Student): ChatCard
{
    var s1: Student = a
    var s2: Student = b
    if (s1.documentId!! > s2.documentId!!)
    {
        s1 = b
        s2 = a
    }
    this.participantsId = listOf(s1.documentId!!, s2.documentId!!)
    this.participantsName = listOf(s1.name, s2.name)
    this.participantsPhoto = listOf(s1.photo, s2.photo)

    return this
}

fun ChatCard.getChatId(): String
{
    return this.participantsId[0]+this.participantsId[1]
}

fun ChatCard.getPartnerId(myUserId: String): String
{
    if (participantsId[0] != myUserId)
        return participantsId[0]
    return participantsId[1]
}

fun ChatCard.getName(userID: String): String
{
    if (participantsId[0] == userID)
        return this.participantsName[0]
    return participantsName[1]
}

fun ChatCard.getPhoto(userID: String): String?
{
    if (participantsId[0] == userID)
        return this.participantsPhoto[0]
    return participantsPhoto[1]
}