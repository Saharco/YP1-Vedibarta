package com.technion.vedibarta.login

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.technion.vedibarta.R
import android.app.ProgressDialog
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.*
import kotlin.ClassCastException


private const val REQ_GOOGLE_SIGN_IN = 1
private const val TAG = "LoginScreenFragment"

class LoginOptionsFragment : Fragment() {
    private lateinit var signInListener : OnSignInButtonClickListener
    private lateinit var signUpWithEmailListener: OnSignUpWithEmailButtonClickListener
    private lateinit var continueWithGoogleListener: OnContinueWithGoogleButtonClickListener

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    private lateinit var callbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        callbackManager = CallbackManager.Factory.create()
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
        signInWithGoogleButton.setOnClickListener { continueWithGoogle() }

        val signInWithFacebookButton = view.findViewById<LoginButton>(R.id.facebook_login_button)
        signInWithFacebookButton.fragment = this
        signInWithFacebookButton.setPermissions("email")
        signInWithFacebookButton.registerCallback(callbackManager, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d(TAG, "facebook:onSuccess $loginResult")
                handleFacebookAccessToken(loginResult.accessToken)
            }
            override fun onCancel() {
                Log.d(TAG, "facebook:onCancel")
            }
            override fun onError(e: FacebookException) {
                Log.d(TAG, "facebook:onError", e)
            }
        })

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        signInListener = context as? OnSignInButtonClickListener ?:
                throw ClassCastException("$context must implement ${OnSignInButtonClickListener::class}")
        signUpWithEmailListener = context as? OnSignUpWithEmailButtonClickListener ?:
                throw ClassCastException("$context must implement ${OnSignUpWithEmailButtonClickListener::class}")
        continueWithGoogleListener = context as? OnContinueWithGoogleButtonClickListener ?:
                throw ClassCastException("$context must implement ${OnContinueWithGoogleButtonClickListener::class}")
    }

    private fun signIn() {
        signInListener.onSignInButtonClick()
    }

    private fun continueWithGoogle() {
        continueWithGoogleListener.onContinueWithGoogleButtonClick()
    }

    private fun signUpWithEmail() {
        signUpWithEmailListener.onSignUpWithEmailButtonClick()
    }

    interface OnSignInButtonClickListener {
        fun onSignInButtonClick()
    }

    interface OnContinueWithGoogleButtonClickListener {
        fun onContinueWithGoogleButtonClick()
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
            Log.d(TAG, "onActivityResult: Google Sign in")
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
        callbackManager.onActivityResult(requestCode, resultCode, data)
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

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG, "handleFacebookAccessToken: ${token.token}")
        val authCredential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(authCredential)
            .addOnCompleteListener(activity!!) {
                if (it.isSuccessful) {
                    Log.d(TAG, "signInWithCredential: success")
                } else {
                    try {
                        throw it.exception!!
                    } catch (e: FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(activity, "Credentials has been malformed or expired",
                            Toast.LENGTH_SHORT).show()
                    } catch (e: FirebaseAuthUserCollisionException) {
                        Toast.makeText(activity, "User with same credentials already exists",
                            Toast.LENGTH_SHORT).show()
                        val email = authCredential.signInMethod
                        var provider = ""; var providers: List<String>? = null
                        try {
                            val resultTask = auth.fetchSignInMethodsForEmail(email)
                            if (!it.isSuccessful)
                                Log.w(TAG, "Task is not successful")
                            else {
                                providers = resultTask.result!!.signInMethods
                            }
                        } catch (nullptrEx: NullPointerException) {
                            Log.w(TAG, "NullPointerException from getSignInMethods")
                        }
                        if (providers == null || providers.isEmpty())
                            Log.w(TAG, "No existing sign in providers")
                        else
                            provider = providers[0]
                        var alertDialogMessage = "Please sign in with your "
                        if (provider.isEmpty())
                            alertDialogMessage += "Other account"
                        else
                            alertDialogMessage += "$provider account"
                        val builder = AlertDialog.Builder(context)
                        builder.setMessage("Please sign in with your $provider account")
                        auth.signOut()
                        LoginManager.getInstance().logOut()
                    } catch (e: Exception) {
                        Toast.makeText(activity, "Authentication failed",
                            Toast.LENGTH_SHORT).show()
                    }
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", it.exception)
                }
            }
    }
}