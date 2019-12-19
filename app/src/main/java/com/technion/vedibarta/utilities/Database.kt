package com.technion.vedibarta.utilities

import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.storage.*
import com.technion.vedibarta.database.DatabaseVersioning
import java.lang.Exception
import java.util.*

class Storage(private val userId: String?)
{
    private val storage = FirebaseStorage.getInstance().reference
    private var path = ""
    fun students(): Storage {
        path += "students/"
        return this
    }
    fun userId(): Storage {
        path += "$userId/"
        return this
    }
    fun otherUser(id: String): Storage
    {
        path += "$id/"
        return this
    }
    fun pictures(): Storage {
        path += "pictures/"
        return this
    }
    fun profilePicture(): StorageReference
    {
        return storage.child("$path/profile_pic")
    }
    fun fileName(name: String): StorageReference
    {
        return storage.child("$path$name")
    }
}

interface ICollectionPath
{
    fun userId():IDocumentPath
    fun otherUserId(id: String): IDocumentPath
    fun chatId(chatId: String): IDocumentPath
    fun message(d: Date): IDocumentPath
    fun systemMessage(d: Date): IDocumentPath
    fun build(): CollectionReference

}
interface IDocumentPath
{
    fun messages(): ICollectionPath
    fun build(): DocumentReference
}
class DocumentsCollections(private val userId: String?)
{
    private val database = DatabaseVersioning.currentVersion.instance
    fun students():ICollectionPath = CollectionPath(database.collection("students"), userId)
    fun chats():ICollectionPath = CollectionPath(database.collection("chats"), userId)
}
private class CollectionPath(private val c: CollectionReference, private val userId: String?):ICollectionPath
{
    override fun systemMessage(d: Date): IDocumentPath = DocumentPath(c.document("sys$d"), userId)
    override fun chatId(chatId: String): IDocumentPath = DocumentPath(c.document(chatId), userId)
    override fun userId() = DocumentPath(c.document("$userId"), userId)
    override fun otherUserId(id: String): IDocumentPath = DocumentPath(c.document(id), userId)
    override fun message(d: Date): IDocumentPath = DocumentPath(c.document(d.toString()), userId)
    override fun build(): CollectionReference = c
}
private class DocumentPath(private val d: DocumentReference, private val userId: String?): IDocumentPath
{
    override fun messages(): ICollectionPath = CollectionPath(d.collection("messages"), userId)
    override fun build(): DocumentReference = d
}

fun error(e: Exception, additional: String = "")
{
    Log.d("wtf", "$additional ${e.message}, cause: ${e.cause?.message}")
}
