package com.technion.vedibarta.login

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.technion.vedibarta.R
import java.lang.ClassCastException


class LoginOptionsFragment : Fragment() {
    private lateinit var signInListener : OnSignInButtonClickListener
    private lateinit var signUpWithEmailListener: OnSignUpWithEmailButtonClickListener

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

        return view
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

    private fun signUpWithEmail() {
        signUpWithEmailListener.onSignUpWithEmailButtonClick()
    }

    interface OnSignInButtonClickListener {
        fun onSignInButtonClick()
    }

    interface OnSignUpWithEmailButtonClickListener {
        fun onSignUpWithEmailButtonClick()
    }
}