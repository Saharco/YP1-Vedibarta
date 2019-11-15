package com.technion.vedibarta.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.technion.vedibarta.R

class ProfilePictureUploadDialog : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val inflatedView = inflater.inflate(R.layout.profile_picture_dialog, container, false)
        inflatedView.setOnClickListener {
            when (it?.id) {
                R.id.cameraUploadButton -> TODO()
                R.id.galleryUploadButton -> TODO()
                R.id.dismissButton -> dismiss()
                else -> dismiss()
            }
            dismiss()
        }
        dialog?.setCanceledOnTouchOutside(true)
        return inflatedView
    }
}
