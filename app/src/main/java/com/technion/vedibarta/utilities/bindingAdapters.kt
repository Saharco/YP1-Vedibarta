package com.technion.vedibarta.utilities

import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import com.google.android.material.textfield.TextInputLayout
import com.technion.vedibarta.POJOs.ValidationResult

@BindingAdapter("validate")
fun validation(
    textInputLayout: TextInputLayout,
    validationLiveData: LiveData<ValidationResult>
) {
    textInputLayout.error = when (val validation = validationLiveData.value) {
        is ValidationResult.Invalid -> textInputLayout.context.getString(validation.resId)
        is ValidationResult.Valid   -> null
        else                        -> null
    }
}

@BindingAdapter("onTextChanged")
fun onTextChanged(
    textView: TextView,
    onTextChangedListener: () -> Unit
) = textView.addTextChangedListener { onTextChangedListener() }
