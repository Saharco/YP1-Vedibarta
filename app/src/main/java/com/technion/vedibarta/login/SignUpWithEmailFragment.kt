package com.technion.vedibarta.login

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

import com.technion.vedibarta.R
import com.technion.vedibarta.utilities.isEmail
import kotlinx.android.synthetic.main.fragment_sign_up_with_email.*
import java.lang.ClassCastException

/**
 * The sign-up with email screen.
 *
 * This class handles the actions taken when any button is pressed on the sign-up with email screen.
 *
 * @see R.layout.fragment_sign_up_with_email
 */
class SignUpWithEmailFragment : Fragment() {
    // Keys to be used when saving and restoring fragment states.
    companion object StatesKeys {
        const val EMAIL_KEY =           "state:email"
        const val PASSWORD_KEY =        "state:password"
        const val PASSWORD_REP_KEY =    "state:password_repeat"
    }

    private lateinit var backListener: OnBackButtonClickListener
    // Notice: the listener will be called only after checking for valid inputs.
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
        // Set up back button listener.
        val backButton = view.findViewById<Button>(R.id.back_button)
        backButton.setOnClickListener { back() }
        // Set up sign-up button listener
        val signUpButton = view.findViewById<Button>(R.id.sign_up_button)
        signUpButton.setOnClickListener {
            // Checking input before calling signUpListener.
            signUp()
        }

        val email = view.findViewById<TextInputEditText>(R.id.email_input_edit_text)
        val emailLayout = view.findViewById<TextInputLayout>(R.id.email_input_layout)
        val password = view.findViewById<TextInputEditText>(R.id.password_input_edit_text)
        val passwordLayout = view.findViewById<TextInputLayout>(R.id.password_input_layout)
        val passwordRepeat = view.findViewById<TextInputEditText>(R.id.password_repeat_input_edit_text)
        val passwordRepeatLayout = view.findViewById<TextInputLayout>(R.id.password_repeat_input_layout)

        // Removing error messages after editing texts.
        email.addTextChangedListener { emailLayout.error = null }
        password.addTextChangedListener { passwordLayout.error = null }
        passwordRepeat.addTextChangedListener { passwordRepeatLayout.error = null }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Refilling edit-texts when restarting the activity.
        if (savedInstanceState != null) {
            email_input_edit_text.setText(savedInstanceState.getString(EMAIL_KEY))
            password_input_edit_text.setText(savedInstanceState.getString(PASSWORD_KEY))
            password_repeat_input_edit_text.setText(savedInstanceState.getString(PASSWORD_REP_KEY))
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // Saving filled texts.
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

        // Calling signUpListener only if there were no errors.
        if ((emailError == null) and (passwordError == null) and (passwordRepError == null)) {
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
