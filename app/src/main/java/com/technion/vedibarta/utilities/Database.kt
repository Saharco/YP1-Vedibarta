package com.technion.vedibarta.utilities

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.technion.vedibarta.POJOs.Student
import java.io.File
import java.sql.Timestamp

class Database {
    private val database = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance().reference
    private val user = FirebaseAuth.getInstance().currentUser
    private val logTag = "DataBase"

    private class StoragePathBuilder(user: FirebaseUser)
    {
        private val userId = user.uid
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

    private class DataBasePathBuilder(private val database: FirebaseFirestore, user: FirebaseUser)
    {
        private val userId = user.uid

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
        DOWNLOAD_PROFILE_PICTURE
    }

    private fun uploadFile(file: Uri, path: String): StorageTask<UploadTask.TaskSnapshot>
    {
        val filePathRef = storage.child(path)
        return filePathRef.putFile(file)
    }

    private fun downloadFile(path: String, destination: Uri): FileDownloadTask
    {
        val filePathRef = storage.child(path)
        return filePathRef.getFile(destination)
    }

    private fun logSuccess(action: Action)
    {
        Log.d(logTag, "${user?.uid} $action success")
    }

    private fun logFailure(e: Exception, action: Action)
    {
        Log.d(logTag, "${user?.uid} $action got Error: ${e.message} the cause: ${e.cause?.message}")
    }


    fun saveStudentProfile(name: String, photo: String?, region: String, school: String, gender: Gender,
                           lastTimeActive: Timestamp, characteristics: List<String>, hobbies: List<String>): com.google.android.gms.tasks.Task<Void>?
    {
        val s = Student(name, photo, region, school, gender, lastTimeActive, characteristics, hobbies)
        var task:com.google.android.gms.tasks.Task<Void>? = null
        if (user != null)
        {
            task = DataBasePathBuilder(database, user).students().userId().build().set(s)
                .addOnSuccessListener { logSuccess(Action.SAVE_PROFILE) }
                .addOnFailureListener{ e:Exception -> logFailure(e, Action.SAVE_PROFILE) }
        }
        return task
    }

    fun uploadProfilePicture(photo: Uri): StorageTask<UploadTask.TaskSnapshot>?
    {
        var task: StorageTask<UploadTask.TaskSnapshot>? = null
        if (user != null)
        {
            val path = StoragePathBuilder(user).students().userId().pictures().fileName("profile_pic")
            Log.d(logTag, "uploading profile picture")
            task = uploadFile(photo, path)
                .addOnSuccessListener { logSuccess(Action.UPLOAD_PROFILE_PICTURE) }
                .addOnFailureListener{ e:Exception -> logFailure(e, Action.UPLOAD_PROFILE_PICTURE) }
        }
        return task
    }

    fun downloadProfilePicture(photoDestination: Uri): StorageTask<FileDownloadTask.TaskSnapshot>?
    {
        var task: StorageTask<FileDownloadTask.TaskSnapshot>? = null
        if (user != null)
        {
            Log.d(logTag, "downloading profile picture")
            val path = StoragePathBuilder(user).students().userId().pictures().fileName("profile_pic")
            task = downloadFile(path, photoDestination)
                .addOnSuccessListener { logSuccess(Action.DOWNLOAD_PROFILE_PICTURE) }
                .addOnFailureListener{ e:Exception -> logFailure(e, Action.DOWNLOAD_PROFILE_PICTURE) }
        }
        return task
    }
}
