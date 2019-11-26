package com.technion.vedibarta.login

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.technion.vedibarta.R


private const val REQ_GOOGLE_SIGN_IN = 1
private const val TAG = "LoginActivity"

class LoginActivity : AppCompatActivity(), LoginOptionsFragment.OnSignInButtonClickListener,
    LoginOptionsFragment.OnSignUpWithEmailButtonClickListener,
    LoginOptionsFragment.OnContinueWithGoogleButtonClickListener,
    LoginFragment.OnBackButtonClickListener, SignUpWithEmailFragment.OnBackButtonClickListener,
    SignUpWithEmailFragment.OnSignUpButtonClickListener {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        // 381465238096-e60campao164cdi8j1bs8pp0h53cs5c1.apps.googleusercontent.com
        // Set up google sign-in client
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("381465238096-e60campao164cdi8j1bs8pp0h53cs5c1.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // If activity is being restored from a previous state, then do nothing.
        if(savedInstanceState != null) {
            return
        }

        val fm = supportFragmentManager
        fm.beginTransaction().apply {
            add(R.id.login_screen_fragment, LoginOptionsFragment())
        }.commit()
    }

    override fun onStart() {
        super.onStart()

        val currentUser = auth.currentUser
        updateUIForCurrentUser(currentUser)
    }

    override fun onSignInButtonClick() {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.login_screen_fragment, LoginFragment())
            addToBackStack(null)
        }.commit()
    }

    override fun onContinueWithGoogleButtonClick() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, REQ_GOOGLE_SIGN_IN)
    }

    override fun onSignUpWithEmailButtonClick() {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.login_screen_fragment, SignUpWithEmailFragment())
            addToBackStack(null)
        }.commit()
    }

    override fun onSignUpButtonClick(email: String, password: String) {
        Toast.makeText(this, "Signed Up", Toast.LENGTH_SHORT).show()
        // TODO: authenticate
    }

    override fun onBackButtonClick() {
        supportFragmentManager.popBackStack()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data == null) {
            Log.w(TAG, "Intent is null")
            return
        }

        if (requestCode == REQ_GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            if (!task.isSuccessful) {
                Log.w(TAG, "Failed to get account from intent")
            }

            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
            } catch (e: Exception) {
                Log.w(TAG, "Caught unexpected exception: $e")
            }
            return
        }
//        supportFragmentManager.findFragmentByTag("LoginScreenFragment")!!
//            .onActivityResult(requestCode, resultCode, data)
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        Log.d(TAG, "firebaseAuthWithGoogle: ${account.id!!}")

        val dialog = ProgressDialog(this).apply {
            setMessage("Loading data...")
            setCancelable(false)
            setIndeterminate(true)
            show()
        }

        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.w(TAG, "signInWithCredential: success")
                    val user = auth.currentUser
                    updateUIForCurrentUser(user)
                } else {
                    // Sign in failed
                    Log.w(TAG, "signInWithCredential: failure", task.exception)
                    Toast.makeText(this, "Auth Failed", Toast.LENGTH_LONG).show()
                }

                dialog.dismiss()
            }
    }

    private fun updateUIForCurrentUser(user: FirebaseUser?) {
        if (user != null) {
            Toast.makeText(this, "!${user.displayName} ,שלום", Toast.LENGTH_LONG).show()
            // TODO: check if the user's document exists.
            //  If so, direct to main screen. Otherwise, direct to profile creation screen.
        }
    }
}
