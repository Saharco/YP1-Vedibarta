package com.technion.vedibarta.utilities

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.*
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
    fun pictures(): Storage {
        path += "pictures/"
        return this
    }
    fun fileName(name: String): StorageReference
    {
        return storage.child("$path$name")
    }
}

interface ICollectionPath
{
    fun userId():IDocumentPath
    fun chatWith(partnerId: String): IDocumentPath
    fun message(d: Date): IDocumentPath
    fun build(): CollectionReference

}
interface IDocumentPath
{
    fun chats(): ICollectionPath
    fun messages(): ICollectionPath
    fun build(): DocumentReference
}
class DocumentsCollections(private val userId: String?)
{
    private val database = FirebaseFirestore.getInstance()
    fun students():ICollectionPath = CollectionPath(database.collection("students"), userId)
    fun chats():ICollectionPath = CollectionPath(database.collection("chats"), userId)
}
private class CollectionPath(private val c: CollectionReference, private val userId: String?):ICollectionPath
{
    override fun chatWith(partnerId: String): IDocumentPath = DocumentPath(c.document(partnerId), userId)
    override fun userId() = DocumentPath(c.document("$userId"), userId)
    override fun message(d: Date): IDocumentPath = DocumentPath(c.document(d.toString()), userId)
    override fun build(): CollectionReference = c
}
private class DocumentPath(private val d: DocumentReference, private val userId: String?): IDocumentPath
{
    override fun chats(): ICollectionPath = CollectionPath(d.collection("chats"), userId)
    override fun messages(): ICollectionPath = CollectionPath(d.collection("messages"), userId)
    override fun build(): DocumentReference = d
}
