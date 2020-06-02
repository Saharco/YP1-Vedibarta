package com.technion.vedibarta.utilities

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import com.github.zagum.expandicon.ExpandIconView
import com.google.android.material.textfield.TextInputLayout
import com.technion.vedibarta.POJOs.ValidationResult
import com.technion.vedibarta.R
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File

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

@BindingAdapter("image")
fun setBackgroundFromFile(civ: CircleImageView, file: File?) {
    civ.setImageDrawable(when (file) {
        null -> ColorDrawable(ContextCompat.getColor(civ.context, R.color.colorAccent))
        else -> Drawable.createFromPath(file.path)
    })
}

@BindingAdapter("state")
fun setState(expandedIconView: ExpandIconView, state: Int) =
    expandedIconView.setState(state, true)
