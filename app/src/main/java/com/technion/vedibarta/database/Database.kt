package com.technion.vedibarta.database

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.storage.*
import com.technion.vedibarta.R
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.timer

/***
 *  This class allows to gradually build a path to access a file in the firebase storage hierarchy
 */
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

/**
 * interface that wraps [CollectionReference] of firebase and is used when building a path to a collection
 */
interface ICollectionPath
{
    fun user(): IDocumentPath
    fun otherUser(id: String): IDocumentPath
    fun chat(id: String): IDocumentPath
    fun `class`(id: String): IDocumentPath
    fun message(d: Date): IDocumentPath
    fun systemMessage(d: Date): IDocumentPath
    fun report(id: String): IDocumentPath
    fun build(): CollectionReference
}

/**
 * interface that wraps [DocumentReference] of firebase and is used when building a path to a document
 */
interface IDocumentPath
{
    fun messages(): ICollectionPath

    //    fun classes(): ICollectionPath
    fun build(): DocumentReference
}

/***
 *  This class in combination with [ICollectionPath] and [IDocumentPath] interfaces allows to
 *  gradually build a path to access a document\collection in the firebase hierarchy
 */
class DataBase(val userId: String)
{
    private val database = DatabaseVersioning.currentVersion.instance
    val clock = Clock()

    private fun collection(name: String): ICollectionPath =
        CollectionPath(database.collection(name), userId)

    fun students(): ICollectionPath = collection("students")

    fun teachers(): ICollectionPath = collection("teachers")

    fun classes(): ICollectionPath = collection("classes")

    fun reports(): ICollectionPath = collection("abuseReports")

    fun chats(): ICollectionPath = collection("chats")

    /**
     * Handles the passage of time in the app
     */
    class Clock
    {
        private var currentDate: Date = Date()
        private val c = GregorianCalendar()

        init
        {
            //update once every 60000/1000 = 60 [seconds]
            timer(period = 60000) {
                updateCurrentDateFromServer()
            }
        }

        fun getCurrentDate() = currentDate

        fun hasMoreThenADayPassed(time: Date): Boolean
        {
            val midnight = c.getMidnightOf(time)
            val timeGapInDays =
                TimeUnit.DAYS.convert(currentDate.time - midnight.time, TimeUnit.MILLISECONDS)
            return timeGapInDays >= 1
        }

        fun calcRelativeTime(time: Date?, context: Context): String
        {
            if (time == null) return ""
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
}

private class CollectionPath(private val c: CollectionReference, private val userId: String?) :
        ICollectionPath
{
    private fun doc(id: String) = DocumentPath(c.document(id), userId)

    override fun systemMessage(d: Date): IDocumentPath = doc("sys$d")

    override fun chat(id: String): IDocumentPath = doc(id)

    override fun `class`(id: String): IDocumentPath = doc(id)

    override fun user(): IDocumentPath = doc("$userId")

    override fun otherUser(id: String): IDocumentPath = doc(id)

    override fun message(d: Date): IDocumentPath = doc(d.toString())

    override fun report(id: String): IDocumentPath = doc(id)

    override fun build(): CollectionReference = c
}

private class DocumentPath(private val d: DocumentReference, private val userId: String?) :
        IDocumentPath
{
    override fun messages(): ICollectionPath = CollectionPath(d.collection("messages"), userId)

    override fun build(): DocumentReference = d
}

fun error(e: Exception, additional: String = "")
{
    Log.d("dberror", "$additional ${e.message}, cause: ${e.cause?.message}")
}
