package com.technion.vedibarta.utilities

import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.technion.vedibarta.POJOs.Student
import java.io.File

class Database {
    private val database = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance().reference
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val logTag = "DataBase"

    private class StoragePathBuilder(private val userId: String)
    {
        private var path = ""

        fun students(): StoragePathBuilder
        {
            path += "students/"
            return this
        }

        fun userId(): StoragePathBuilder
        {
            path += "$userId/"
            return this
        }

        fun pictures(): StoragePathBuilder
        {
            path += "pictures/"
            return this
        }

        fun fileName(name: String): String
        {
            return "$path$name"
        }
    }

    private class DataBasePathBuilder(private val database: FirebaseFirestore, private val userId: String)
    {
        fun students() = CollectionPath(database.collection("students"), userId)

        fun chats() = CollectionPath(database.collection("chats"), userId)
    }

    private class CollectionPath(private val c: CollectionReference, private val userId: String)
    {
        fun userId() = DocumentPath(c.document(userId), userId)

    }

    private class DocumentPath(private val d: DocumentReference, private val userId: String)
    {
        fun build() = d

        fun chats() = CollectionPath(d.collection("chats"), userId)
    }

    private enum class Action
    {
        SAVE_PROFILE,
        UPLOAD_PROFILE_PICTURE,
        DOWNLOAD_PROFILE_PICTURE,
        GET_PROFILE
    }

    private fun uploadFile(file: Uri, path: String): StorageTask<UploadTask.TaskSnapshot>
    {
        val filePathRef = storage.child(path)
        return filePathRef.putFile(file)
    }

    private fun downloadFile(path: String, destination: File): FileDownloadTask
    {
        val filePathRef = storage.child(path)
        return filePathRef.getFile(destination)
    }

    private fun logSuccess(a: Action)
    {
        Log.d(logTag, "$userId $a success")
    }

    private fun logFailure(e: Exception, a: Action)
    {
        Log.d(logTag, "$userId $a got Error: ${e.message} the cause: ${e.cause?.message}")
    }

    private fun logStart(a: Action)
    {
        Log.d(logTag, "started $a")
    }


    fun saveStudentProfile(s: Student): Task<Void>?
    {
        var task:Task<Void>? = null
        if (userId != null)
        {
            logStart(Action.SAVE_PROFILE)
            task = DataBasePathBuilder(database, userId).students().userId().build().set(s)
                .addOnSuccessListener { logSuccess(Action.SAVE_PROFILE) }
                .addOnFailureListener{ e:Exception -> logFailure(e, Action.SAVE_PROFILE) }
        }
        return task
    }

    //to get a Student object you will need to use document.toObject(Student::class.java)
    //when document is the parameter of the lambda expression task.addOnSuccessListener{document ->}
    fun getStudentProfile(): Task<DocumentSnapshot>?
    {
        var task: Task<DocumentSnapshot>? = null
        if (userId != null)
        {
            logStart(Action.GET_PROFILE)
            task = DataBasePathBuilder(database, userId).students().userId().build().get()
                .addOnSuccessListener { logSuccess(Action.GET_PROFILE) }
                .addOnFailureListener{ e:Exception -> logFailure(e, Action.GET_PROFILE) }
        }
        return task
    }

    fun uploadProfilePicture(photo: Uri): StorageTask<UploadTask.TaskSnapshot>?
    {
        var task: StorageTask<UploadTask.TaskSnapshot>? = null
        if (userId != null)
        {
            val path = StoragePathBuilder(userId).students().userId().pictures().fileName("profile_pic")
            logStart(Action.UPLOAD_PROFILE_PICTURE)
            task = uploadFile(photo, path)
                .addOnSuccessListener { logSuccess(Action.UPLOAD_PROFILE_PICTURE) }
                .addOnFailureListener{ e:Exception -> logFailure(e, Action.UPLOAD_PROFILE_PICTURE) }
        }
        return task
    }

    fun downloadProfilePicture(destination: File): StorageTask<FileDownloadTask.TaskSnapshot>?
    {
        var task: StorageTask<FileDownloadTask.TaskSnapshot>? = null
        if (userId != null)
        {
            logStart(Action.DOWNLOAD_PROFILE_PICTURE)
            val path = StoragePathBuilder(userId).students().userId().pictures().fileName("profile_pic")
            task = downloadFile(path, destination)
                .addOnSuccessListener {
                    logSuccess(Action.DOWNLOAD_PROFILE_PICTURE)
                }
                .addOnFailureListener {
                    logFailure(it, Action.DOWNLOAD_PROFILE_PICTURE)
                }
        }
        return task
    }
}
