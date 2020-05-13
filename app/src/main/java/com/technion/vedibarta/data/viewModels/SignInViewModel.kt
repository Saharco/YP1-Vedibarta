package com.technion.vedibarta.data.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.technion.vedibarta.POJOs.ValidationResult
import com.technion.vedibarta.R
import com.technion.vedibarta.utilities.isEmail

class SignInViewModel(state: SavedStateHandle): ViewModel() {
    companion object StateKeys {
        const val EMAIL_KEY                  = "email"
        const val PASSWORD_KEY               = "password"
        const val DISPLAY_EMAIL_ERROR_KEY    = "display_email_error"
        const val DISPLAY_PASSWORD_ERROR_KEY = "display_password_error"
    }

    val email    = state.getLiveData(EMAIL_KEY,    "")
    val password = state.getLiveData(PASSWORD_KEY, "")

    fun showEmailError() {
        _showEmailError.value = true
    }

    fun showPasswordError() {
        _showPasswordError.value = true
    }

    fun hideEmailError() {
        _showEmailError.value = false
    }

    fun hidePasswordError() {
        _showPasswordError.value = false
    }

    private val _showEmailError    = state.getLiveData(DISPLAY_EMAIL_ERROR_KEY,    false)
    private val _showPasswordError = state.getLiveData(DISPLAY_PASSWORD_ERROR_KEY, false)

    // the validation status of the email input, not necessarily displayed
    private val emailValidation = Transformations.map(email) {
        when {
            it.isBlank()  -> ValidationResult.Invalid(R.string.blank_input_error)
            !it.isEmail() -> ValidationResult.Invalid(R.string.email_format_error)
            else          -> ValidationResult.Valid
        }
    }

    // the validation status of the password input, not necessarily displayed
    private val passwordValidation = Transformations.map(password) {
        when {
            it.isBlank() -> ValidationResult.Invalid(R.string.blank_input_error)
            else         -> ValidationResult.Valid
        }
    }

    // the validation status to be displayed
    val displayedEmailValidation = Transformations.switchMap(_showEmailError) {
        if (it)
            emailValidation
        else
            MutableLiveData<ValidationResult>(
                ValidationResult.Valid)
    }

    // the validation status to be displayed
    val displayedPasswordValidation = Transformations.switchMap(_showPasswordError) {
        if (it)
            passwordValidation
        else
            MutableLiveData<ValidationResult>(
                ValidationResult.Valid)
    }

    fun validate(): Boolean {
        showEmailError()
        showPasswordError()

        return emailValidation.value is ValidationResult.Valid
                && passwordValidation.value is ValidationResult.Valid
    }
}
