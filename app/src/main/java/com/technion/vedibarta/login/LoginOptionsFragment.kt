package com.technion.vedibarta.login

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.technion.vedibarta.R
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
import com.google.firebase.auth.*
import kotlin.ClassCastException


private const val TAG = "LoginScreenFragment"

class LoginOptionsFragment : Fragment() {
    private lateinit var signInListener : OnSignInButtonClickListener
    private lateinit var signUpWithEmailListener: OnSignUpWithEmailButtonClickListener
    private lateinit var continueWithGoogleListener: OnContinueWithGoogleButtonClickListener

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
        Log.d(TAG, "onActivityResult invoked")
        callbackManager.onActivityResult(requestCode, resultCode, data)
        //super.onActivityResult(requestCode, resultCode, data)
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG, "handleFacebookAccessToken: ${token.token}")
        val authCredential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(authCredential)
            .addOnCompleteListener(activity!!) {
                if (it.isSuccessful) {
                    Log.d(TAG, "signInWithCredential: success")
                    (activity as LoginActivity).updateUIForCurrentUser(auth.currentUser)
                } else {
                    try {
                        throw it.exception!!
                    } catch (e: FirebaseAuthInvalidCredentialsException) {
                        Log.d(TAG, "Credentials has been malformed or expired")
                        Toast.makeText(activity, R.string.sign_in_error, Toast.LENGTH_SHORT).show()
                    } catch (e: FirebaseAuthUserCollisionException) {
                        Log.d(TAG, "User with same credentials already exists")
                        Toast.makeText(activity, R.string.sign_in_error, Toast.LENGTH_SHORT).show()
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
                        auth.signOut()
                        LoginManager.getInstance().logOut()
                    } catch (e: Exception) {
                        Log.d(TAG, "Authentication failed")
                        Toast.makeText(activity, R.string.sign_in_error, Toast.LENGTH_SHORT).show()
                    }
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", it.exception)
                }
            }
    }
}