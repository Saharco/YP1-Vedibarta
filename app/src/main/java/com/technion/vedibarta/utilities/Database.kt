package com.technion.vedibarta.utilities

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.technion.vedibarta.POJOs.Student
import java.lang.Exception
import java.sql.Timestamp

class Database
{
    private val database = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance().reference
    private val user = FirebaseAuth.getInstance().currentUser
    private val studentsDocumentsCollection = "students"
    private val studentsStorageCollection = "students"


    fun saveStudentProfile(name: String, photo: String?, region: String, shcool: String, gender: Gender,
                            lastTimeActive: Timestamp, characteristics: List<String>, hobbies: List<String>)
    {
        val s = Student(name,photo, region, shcool, gender, lastTimeActive, characteristics, hobbies)

        try {
            if (user != null)
                database.collection(studentsDocumentsCollection).document(user.uid).set(s)
                    .addOnSuccessListener { Log.d("Database", "saved profile of ${user.uid}")}
                    .addOnFailureListener { e: Exception -> Log.d("Database", "${user.uid} got error: ${e.message}") }
        }
        catch (e: Exception)
        {
            Log.d("Database", "${user?.uid} got error: ${e.message}")
        }
    }

    fun uploadProfilePicture(photo: Uri)
    {
        if (user != null)
        {
            val path = PathBuilder(user).students().userId().pictures().fileName("profile_pic")
            uploadFile(photo, path)
        }
    }

    private fun uploadFile(file: Uri, path: String)
    {
        if (user != null)
        {
            val photoPathRef = storage.child(path)
            val uploadTask = photoPathRef.putFile(file)
            uploadTask.addOnSuccessListener { Log.d("Database", "${user.uid} uploaded a file") }
                .addOnFailureListener{ e: Exception -> Log.d("Database", "${user.uid} got error: ${e.message}") }
        }
    }
}

class PathBuilder(user: FirebaseUser)
{
    private val userId = user.uid
    private var path = ""

    fun students(): PathBuilder
    {
        path += "students/"
        return this
    }

    fun userId(): PathBuilder
    {
        path += "$userId/"
        return this
    }

    fun pictures(): PathBuilder
    {
        path += "pictures/"
        return this
    }

    fun fileName(name: String): String
    {
        return "$path$name"
    }
}
