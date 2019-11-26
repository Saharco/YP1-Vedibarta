package com.technion.vedibarta.login

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

import com.technion.vedibarta.R
import com.technion.vedibarta.utilities.isEmail
import kotlinx.android.synthetic.main.fragment_sign_up_with_email.*
import java.lang.ClassCastException


class SignUpWithEmailFragment : Fragment() {
    // Keys to be used when saving and restoring fragment states.
    companion object StatesKeys {
        const val EMAIL_KEY =           "state:email"
        const val PASSWORD_KEY =        "state:password"
        const val PASSWORD_REP_KEY =    "state:password_repeat"
    }

    private lateinit var backListener: OnBackButtonClickListener
    private lateinit var signUpListener: OnSignUpButtonClickListener

    override fun onAttach(context: Context) {
        super.onAttach(context)

        backListener = context as? OnBackButtonClickListener ?:
                throw ClassCastException("$context must implement ${OnBackButtonClickListener::class}")
        signUpListener = context as? OnSignUpButtonClickListener ?:
                throw ClassCastException("$context must implement ${OnSignUpButtonClickListener::class}")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_sign_up_with_email, container, false)

        val backButton = view.findViewById<Button>(R.id.back_button)
        backButton.setOnClickListener { back() }

        val signUpButton = view.findViewById<Button>(R.id.sign_up_button)
        signUpButton.setOnClickListener { signUp() }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (savedInstanceState != null) {
            email_input_edit_text.setText(savedInstanceState.getString(EMAIL_KEY))
            password_input_edit_text.setText(savedInstanceState.getString(PASSWORD_KEY))
            password_repeat_input_edit_text.setText(savedInstanceState.getString(PASSWORD_REP_KEY))
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(email_input_edit_text.text.toString(), EMAIL_KEY)
        outState.putString(password_input_edit_text.text.toString(), PASSWORD_KEY)
        outState.putString(password_repeat_input_edit_text.text.toString(), PASSWORD_REP_KEY)
    }

    private fun back() {
        backListener.onBackButtonClick()
    }

    private fun signUp() {
        val email = email_input_edit_text.text.toString()
        val password = password_input_edit_text.text.toString()
        val passwordRep = password_repeat_input_edit_text.text.toString()

        val emailError = when {
            email.isBlank() ->          getString(R.string.blank_input_error)
            !email.isEmail() ->         getString(R.string.email_format_error)
            else ->                     null
        }

        val passwordError = when {
            password.isBlank() ->       getString(R.string.blank_input_error)
            password.length < 8 ->      getString(R.string.short_password_error)
            else ->                     null
        }

        val passwordRepError = when {
            passwordRep.isBlank() ->    getString(R.string.blank_input_error)
            passwordRep != password ->  getString(R.string.wrong_password_repeat)
            else ->                     null
        }

        email_input_layout.error = emailError
        password_input_layout.error = passwordError
        password_repeat_input_layout.error = passwordRepError

        if ((emailError == null) and (passwordError == null) and (passwordRepError == null)){
            signUpListener.onSignUpButtonClick(email, password)
        }
    }

    interface OnSignUpButtonClickListener {
        fun onSignUpButtonClick(email: String, password: String)
    }

    interface OnBackButtonClickListener {
        fun onBackButtonClick()
    }
}
