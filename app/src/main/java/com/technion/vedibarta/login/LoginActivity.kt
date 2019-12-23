package com.technion.vedibarta.login

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.technion.vedibarta.POJOs.Student
import com.technion.vedibarta.R
import com.technion.vedibarta.main.MainActivity
import com.technion.vedibarta.utilities.DocumentsCollections
import com.technion.vedibarta.utilities.VedibartaActivity
import com.technion.vedibarta.utilities.extensions.isInForeground


private const val REQ_GOOGLE_SIGN_IN = 1
private const val TAG = "LoginActivity"

class LoginActivity : AppCompatActivity(), LoginOptionsFragment.OnSignInButtonClickListener,
    LoginOptionsFragment.OnSignUpWithEmailButtonClickListener,
    LoginOptionsFragment.OnContinueWithGoogleButtonClickListener,
    LoginFragment.OnBackButtonClickListener, LoginFragment.OnLoginButtonClickListener,
    SignUpWithEmailFragment.OnBackButtonClickListener,
    SignUpWithEmailFragment.OnSignUpButtonClickListener {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    companion object {
        const val MINIMUM_LOAD_TIME = 1000L
    }

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
        if (savedInstanceState != null) {
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
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Toast.makeText(this, task.exception!!.message, Toast.LENGTH_LONG).show()
                } else {
                    val user = auth.currentUser
                    sendEmailVerification(user!!)
                }
            }
    }

    private fun sendEmailVerification(user: FirebaseUser) {
        val dialog = ProgressDialog(this).apply {
            setMessage(getString(R.string.sending_verification_email))
            setCancelable(false)
            setIndeterminate(true)
            show()
        }

        user.sendEmailVerification()
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "Failed to send verification email")
                    Toast.makeText(
                        this, getString(R.string.something_went_wrong),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Log.w(TAG, "Verification email sent")
                    Toast.makeText(
                        this, getString(R.string.email_sent_successfully),
                        Toast.LENGTH_LONG
                    ).show()
                    onBackButtonClick()
                }

                dialog.dismiss()
            }
    }

    override fun onLoginButtonClick(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "Failed to sign in with email")
                    Toast.makeText(this, task.exception!!.message, Toast.LENGTH_LONG).show()
                } else {
                    val user = auth.currentUser
                    if (user!!.isEmailVerified) {
                        updateUIForCurrentUser(user)
                    } else {
                        Toast.makeText(
                            this, getString(R.string.email_not_verified_error),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
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
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        Log.d(TAG, "firebaseAuthWithGoogle: ${account.id!!}")

        val dialog = ProgressDialog(this).apply {
            setMessage(getString(R.string.progress_dialog_loading))
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

    fun updateUIForCurrentUser(user: FirebaseUser?) {
        if (user != null && user.isEmailVerified) {
            val database = DocumentsCollections(user.uid)
            database.students().userId().build().get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    VedibartaActivity.student = document.toObject(Student::class.java)

                    Handler().postDelayed({
                        if (this@LoginActivity.isInForeground()) {
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                    }, MINIMUM_LOAD_TIME)
                } else {
                    Handler().postDelayed({
                        if (this@LoginActivity.isInForeground()) {
                            startActivity(Intent(this, UserSetupActivity::class.java))
                            finish()
                        }
                    }, MINIMUM_LOAD_TIME)
                }
            }

            // drops current layout
            VedibartaActivity.showSplash(
                this,
                getString(R.string.default_loading_message)
            )
        }
    }
}
