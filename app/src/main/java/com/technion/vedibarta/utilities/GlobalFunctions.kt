package com.technion.vedibarta.utilities

import android.content.Context
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.style.AlignmentSpan
import android.text.style.RelativeSizeSpan
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import com.technion.vedibarta.R

fun missingDetailsDialog(context: Context, msg: String) {
    val message = SpannableStringBuilder()
        .bold {
            append(msg).setSpan(
                RelativeSizeSpan(1f),
                0,
                msg.length,
                0
            )
        }

    val text = context.resources.getString(R.string.error)

    val titleText = SpannableStringBuilder()
        .bold { append(text).setSpan(RelativeSizeSpan(1.2f), 0, text.length, 0) }
    titleText.setSpan(AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, text.length, 0)

    val positiveButtonText = SpannableStringBuilder()
        .bold { append(context.resources.getString(R.string.ok)) }

    val builder = AlertDialog.Builder(context)
    builder
        .setTitle(titleText)
        .setIcon(ContextCompat.getDrawable(context, R.drawable.ic_error))
        .setMessage(message)
        .setPositiveButton(positiveButtonText) { _, _ -> }

    builder.create().show()
}