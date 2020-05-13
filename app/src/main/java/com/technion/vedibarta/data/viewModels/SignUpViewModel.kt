package com.technion.vedibarta.data.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.technion.vedibarta.POJOs.ValidationResult
import com.technion.vedibarta.R
import com.technion.vedibarta.utilities.isEmail

class SignUpViewModel(state: SavedStateHandle): ViewModel() {
    companion object StateKeys {
        const val EMAIL_KEY                  = "email"
        const val PASSWORD_KEY               = "password"
        const val REPEAT_KEY                 = "repeat"
        const val DISPLAY_EMAIL_ERROR_KEY    = "display_email_error"
        const val DISPLAY_PASSWORD_ERROR_KEY = "display_password_error"
        const val DISPLAY_REPEAT_ERROR_KEY   = "display_repeat_error"
    }

    val email    = state.getLiveData<String>(EMAIL_KEY,    null)
    val password = state.getLiveData<String>(PASSWORD_KEY, null)
    val repeat   = state.getLiveData<String>(REPEAT_KEY,   null)

    fun showEmailError() {
        _showEmailError.value = true
    }

    fun showPasswordError() {
        _showPasswordError.value = true
    }

    fun showRepeatError() {
        _showRepeatError.value = true
    }

    fun hideEmailError() {
        _showEmailError.value = false
    }

    fun hidePasswordAndRepeatErrors() {
        _showPasswordError.value = false
        _showRepeatError.value = false
    }

    fun hideRepeatError() {
        _showRepeatError.value = false
    }

    private val _showEmailError    = state.getLiveData(DISPLAY_EMAIL_ERROR_KEY,    false)
    private val _showPasswordError = state.getLiveData(DISPLAY_PASSWORD_ERROR_KEY, false)
    private val _showRepeatError   = state.getLiveData(DISPLAY_REPEAT_ERROR_KEY  , false)

    // the validation status of the email input, not necessarily displayed
    private val emailValidation = Transformations.map(email) {
        when {
            it == null    -> ValidationResult.Invalid(R.string.blank_input_error)
            it.isBlank()  -> ValidationResult.Invalid(R.string.blank_input_error)
            !it.isEmail() -> ValidationResult.Invalid(R.string.email_format_error)
            else          -> ValidationResult.Valid
        }
    }

    // the validation status of the password input, not necessarily displayed
    private val passwordValidation = Transformations.map(password) {
        when {
            it == null    -> ValidationResult.Invalid(R.string.blank_input_error)
            it.isBlank()  -> ValidationResult.Invalid(R.string.blank_input_error)
            it.length < 8 -> ValidationResult.Invalid(R.string.short_password_error)
            else          -> ValidationResult.Valid
        }
    }

    // the validation status of the password confirmation input, not necessarily displayed
    private val repeatValidation = Transformations.switchMap(repeat) { repeat ->
        Transformations.map(password) { password ->
            when {
                repeat == null     -> ValidationResult.Invalid(R.string.blank_input_error)
                repeat.isBlank()   -> ValidationResult.Invalid(R.string.blank_input_error)
                repeat != password -> ValidationResult.Invalid(R.string.wrong_password_repeat)
                else               -> ValidationResult.Valid
            }
        }
    }

    // the validation status to be displayed
    val displayedEmailValidation = Transformations.switchMap(_showEmailError) {
        if (it)
            emailValidation
        else
            MutableLiveData<ValidationResult>(ValidationResult.Valid)
    }

    // the validation status to be displayed
    val displayedPasswordValidation = Transformations.switchMap(_showPasswordError) {
        if (it)
            passwordValidation
        else
            MutableLiveData<ValidationResult>(ValidationResult.Valid)
    }

    // the validation status to be displayed
    val displayedRepeatValidation = Transformations.switchMap(_showRepeatError) {
        if (it)
            repeatValidation
        else
            MutableLiveData<ValidationResult>(ValidationResult.Valid)
    }

    fun validate(): Boolean {
        showEmailError()
        showPasswordError()
        showRepeatError()

        return emailValidation.value is ValidationResult.Valid
                && passwordValidation.value is ValidationResult.Valid
                && repeatValidation.value is ValidationResult.Valid
    }
}