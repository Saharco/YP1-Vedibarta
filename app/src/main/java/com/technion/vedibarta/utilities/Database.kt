package com.technion.vedibarta.utilities

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.*

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
    fun build(): CollectionReference
    fun userId():IDocumentPath
}
interface IDocumentPath
{
    fun build(): DocumentReference
    fun chatWith(partnerId: String): ICollectionPath
}
class DocumentsCollections(private val userId: String?)
{
    private val database = FirebaseFirestore.getInstance()
    fun students():ICollectionPath = CollectionPath(database.collection("students"), userId)
    fun chats():ICollectionPath = CollectionPath(database.collection("chats"), userId)
}
private class CollectionPath(private val c: CollectionReference, private val userId: String?):ICollectionPath
{
    override fun build(): CollectionReference = c
    override fun userId() = DocumentPath(c.document("$userId"), userId)
}
private class DocumentPath(private val d: DocumentReference, private val userId: String?): IDocumentPath
{
    override fun build(): DocumentReference = d
    override fun chatWith(partnerId: String): ICollectionPath = CollectionPath(d.collection(partnerId), userId)
}
