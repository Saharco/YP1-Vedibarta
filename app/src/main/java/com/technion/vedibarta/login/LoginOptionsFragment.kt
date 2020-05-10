package com.technion.vedibarta.login

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.technion.vedibarta.R
import android.content.Intent
import android.util.Log
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.login.LoginResult
import com.technion.vedibarta.databinding.FragmentLoginOptionsBinding
import kotlin.ClassCastException


private const val TAG = "LoginScreenFragment"

/**
 * The main login screen.
 *
 * This class handles the actions taken when any button is pressed on the main login screen.
 *
 * @see R.layout.fragment_login_options
 */
class LoginOptionsFragment : Fragment() {
    private var _binding: FragmentLoginOptionsBinding? = null
    // binding is only available between onCreateView and onDestroyView
    private val binding get() = _binding!!

    // Buttons listeners.
    private lateinit var signInListener : OnSignInButtonClickListener
    private lateinit var signUpWithEmailListener: OnSignUpWithEmailButtonClickListener
    private lateinit var continueWithGoogleListener: OnContinueWithGoogleButtonClickListener
    private lateinit var continueWithFacebookCallback: OnContinueWithFacebookCallback

    private lateinit var callbackManager: CallbackManager

    override fun onAttach(context: Context) {
        super.onAttach(context)

        signInListener = context as? OnSignInButtonClickListener ?:
                throw ClassCastException("$context must implement ${OnSignInButtonClickListener::class}")
        signUpWithEmailListener = context as? OnSignUpWithEmailButtonClickListener ?:
                throw ClassCastException("$context must implement ${OnSignUpWithEmailButtonClickListener::class}")
        continueWithGoogleListener = context as? OnContinueWithGoogleButtonClickListener ?:
                throw ClassCastException("$context must implement ${OnContinueWithGoogleButtonClickListener::class}")
        continueWithFacebookCallback = context as? OnContinueWithFacebookCallback ?:
                throw ClassCastException("$context must implement ${OnContinueWithFacebookCallback::class}")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        callbackManager = CallbackManager.Factory.create()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginOptionsBinding.inflate(inflater, container, false)

        // Set up login button listener.
        binding.signInLink.setOnClickListener { signIn() }
        // Set up sign-up with email listener.
        binding.signUpWithEmailButton.setOnClickListener { signUpWithEmail() }
        // Set up sign-in with google listener.
        binding.googleLoginButton.setOnClickListener { continueWithGoogle() }
        // Set up sign-in with facebook listener.
        val signInWithFacebookButton = binding.facebookLoginButton
        signInWithFacebookButton.fragment = this
        signInWithFacebookButton.setPermissions("email")
        signInWithFacebookButton.registerCallback(callbackManager, getFacebookCallbackForLogin())

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun signIn() {
        signInListener.onSignInButtonClick()
    }

    private fun continueWithGoogle() {
        continueWithGoogleListener.onContinueWithGoogleButtonClick()
    }

    private fun getFacebookCallbackForLogin() : FacebookCallback<LoginResult> {
        return continueWithFacebookCallback.getCallbackForLogin()
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

    interface OnContinueWithFacebookCallback {
        fun getCallbackForLogin(): FacebookCallback<LoginResult>
    }

    interface OnSignUpWithEmailButtonClickListener {
        fun onSignUpWithEmailButtonClick()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(TAG, "onActivityResult invoked")
        callbackManager.onActivityResult(requestCode, resultCode, data)
        //super.onActivityResult(requestCode, resultCode, data)
    }
}