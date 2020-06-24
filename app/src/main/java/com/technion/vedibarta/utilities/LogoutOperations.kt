package com.technion.vedibarta.utilities

import android.util.Log
import com.facebook.login.LoginManager
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.iid.FirebaseInstanceId
import com.technion.vedibarta.main.MainActivity

fun logout(): Task<Boolean> {
    return FirebaseInstanceId.getInstance().instanceId.continueWith {
        if (!it.isSuccessful) {
            Log.d(MainActivity.TAG, "logout failed")
            return@continueWith false
        }
        val token = it.result?.token
        Log.d(MainActivity.TAG, "token is: $token")
        VedibartaActivity.database.students().user().build()
            .update("tokens", FieldValue.arrayRemove(token))

        FirebaseAuth.getInstance().signOut()
        LoginManager.getInstance().logOut()
        return@continueWith true
    }
}