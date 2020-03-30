package com.technion.vedibarta.utilities

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.storage.*
import com.technion.vedibarta.R
import com.technion.vedibarta.database.DatabaseVersioning
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.timer

class Storage(private val userId: String?)
{
    private val storage = FirebaseStorage.getInstance().reference
    private var path = ""
    fun students(): Storage
    {
        path += "students/"
        return this
    }

    fun userId(): Storage
    {
        path += "$userId/"
        return this
    }

    fun otherUser(id: String): Storage
    {
        path += "$id/"
        return this
    }

    fun pictures(): Storage
    {
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
    fun userId(): IDocumentPath
    fun otherUserId(id: String): IDocumentPath
    fun chatId(chatId: String): IDocumentPath
    fun classId(classId: String): IDocumentPath
    fun message(d: Date): IDocumentPath
    fun systemMessage(d: Date): IDocumentPath
    fun build(): CollectionReference

}

interface IDocumentPath
{
    fun messages(): ICollectionPath
    fun classes(): ICollectionPath
    fun build(): DocumentReference
}

/***
 * must set userId field before using.
 * userId is set in LogInActivity after verification of user
 */
class DataBase
{
    var userId = "illegalUid"
    private val database = DatabaseVersioning.currentVersion.instance
    private var currentDate: Date = Date()
    private val c = GregorianCalendar()

    init
    {
        //update once every 60000/1000 = 60 [seconds]
        timer(period = 60000) {
            updateCurrentDateFromServer()
        }
    }

    fun students(): ICollectionPath = CollectionPath(database.collection("students"), userId)

    fun teachers(): ICollectionPath = CollectionPath(database.collection("teachers"), userId)

    fun chats(): ICollectionPath = CollectionPath(database.collection("chats"), userId)

    fun getCurrentDate() = currentDate

    fun hasMoreThenADayPassed(time: Date): Boolean
    {
        val midnight = c.getMidnightOf(time)
        val timeGapInDays =
            TimeUnit.DAYS.convert(currentDate.time - midnight.time, TimeUnit.MILLISECONDS)
        return timeGapInDays >= 1
    }

    fun calcRelativeTime(time: Date, context: Context): String
    {
        val hasADayPassed = hasMoreThenADayPassed(time)

        if (!hasADayPassed) return SimpleDateFormat("HH:mm", Locale.getDefault()).format(time)
        else
        {
            val midnight = c.getMidnightOf(time)
            val timeGapInDays =
                TimeUnit.DAYS.convert(currentDate.time - midnight.time, TimeUnit.MILLISECONDS)
            if (timeGapInDays == 1L)
                return context.resources.getString(R.string.yesterday)
            else
                return SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(time)
        }
    }

    //calculates the midnight of the the day of the parameter time
    private fun Calendar.getMidnightOf(time: Date): Date
    {
        this.time = time
        this.set(Calendar.HOUR_OF_DAY, 0)
        this.set(Calendar.MINUTE, 0)
        this.set(Calendar.SECOND, 0)
        return this.time
    }

    private fun updateCurrentDateFromServer()
    {
        currentDate = Date() //TODO replace with server timestamp query
    }
}

private class CollectionPath(private val c: CollectionReference, private val userId: String?) :
    ICollectionPath
{
    override fun systemMessage(d: Date): IDocumentPath = DocumentPath(c.document("sys$d"), userId)
    override fun chatId(chatId: String): IDocumentPath = DocumentPath(c.document(chatId), userId)
    override fun classId(classId: String): IDocumentPath = DocumentPath(c.document(classId), userId)
    override fun userId() = DocumentPath(c.document("$userId"), userId)
    override fun otherUserId(id: String): IDocumentPath = DocumentPath(c.document(id), userId)
    override fun message(d: Date): IDocumentPath = DocumentPath(c.document(d.toString()), userId)
    override fun build(): CollectionReference = c
}

private class DocumentPath(private val d: DocumentReference, private val userId: String?) :
    IDocumentPath
{
    override fun messages(): ICollectionPath = CollectionPath(d.collection("messages"), userId)
    override fun classes(): ICollectionPath = CollectionPath(d.collection("classes"), userId)
    override fun build(): DocumentReference = d
}

fun error(e: Exception, additional: String = "")
{
    Log.d("wtf", "$additional ${e.message}, cause: ${e.cause?.message}")
}
