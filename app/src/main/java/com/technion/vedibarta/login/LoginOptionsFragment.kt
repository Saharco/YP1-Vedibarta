package com.technion.vedibarta.login

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.technion.vedibarta.R
import java.lang.ClassCastException
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import android.app.ProgressDialog
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import android.content.Intent
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider


private const val REQ_GOOGLE_SIGN_IN = 1
private const val TAG = "LoginScreenFragment"

class LoginOptionsFragment : Fragment() {
    private lateinit var signInListener : OnSignInButtonClickListener
    private lateinit var signUpWithEmailListener: OnSignUpWithEmailButtonClickListener

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 381465238096-e60campao164cdi8j1bs8pp0h53cs5c1.apps.googleusercontent.com
        // Set up google sign-in client
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("381465238096-e60campao164cdi8j1bs8pp0h53cs5c1.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(activity!!, gso)

        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_login_options, container, false)

        // Set up login button listener.
        val loginButton = view.findViewById<Button>(R.id.sign_in_link)
        loginButton.setOnClickListener { signIn() }
        // Set up sign-up with email listener.
        val signUpWithEmailButton = view.findViewById<Button>(R.id.sign_up_with_email_button)
        signUpWithEmailButton.setOnClickListener { signUpWithEmail() }
        // Set up sign-in with google listener.
        val signInWithGoogleButton = view.findViewById<Button>(R.id.google_login_button)
        signInWithGoogleButton.setOnClickListener { signInWithGoogle() }

        return view
    }

    override fun onStart() {
        super.onStart()

        val account = GoogleSignIn.getLastSignedInAccount(activity!!)
        if(account != null) {
            // TODO: go to main screen
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        signInListener = context as? OnSignInButtonClickListener ?:
                throw ClassCastException("$context must implement ${OnSignInButtonClickListener::class}")
        signUpWithEmailListener = context as? OnSignUpWithEmailButtonClickListener ?:
                throw ClassCastException("$context must implement ${OnSignUpWithEmailButtonClickListener::class}")
    }

    private fun signIn() {
        signInListener.onSignInButtonClick()
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, REQ_GOOGLE_SIGN_IN)
    }

    private fun signUpWithEmail() {
        signUpWithEmailListener.onSignUpWithEmailButtonClick()
    }

    interface OnSignInButtonClickListener {
        fun onSignInButtonClick()
    }

    interface OnSignUpWithEmailButtonClickListener {
        fun onSignUpWithEmailButtonClick()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
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
                val idToken = account.idToken
                if (idToken == null)
                    Log.w(TAG, "ID Token is null")

                // Successful google sign in
                handleGoogleAccount(account)

            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
            } catch (e: Exception) {
                Log.w(TAG, "Caught unexpected exception: $e")
            }

            return
        }
    }

    private fun handleGoogleAccount(account: GoogleSignInAccount) {
        Log.d(TAG, "handleGoogleAccount: ${account.id!!}")

        val dialog = ProgressDialog(activity).apply {
            setMessage("Loading data...")
            setCancelable(false)
            setIndeterminate(true)
            show()
        }

        val credential = GoogleAuthProvider.getCredential(account.idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(activity!!) {
                if (it.isSuccessful) {
                    Log.w(TAG, "signInWithCredential: Success")
                } else {
                    // Sign in failed
                    Log.w(TAG, "signInWithCredential: failure", it.exception)
                }
            }

        dialog.cancel()
    }
}