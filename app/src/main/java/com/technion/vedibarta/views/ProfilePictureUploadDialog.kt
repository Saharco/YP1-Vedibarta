package com.technion.vedibarta.views

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.technion.vedibarta.R
import kotlinx.android.synthetic.main.profile_picture_dialog.*

class ProfilePictureUploadDialog : DialogFragment() {

    private lateinit var listener: ProfilePictureUploadDialogListener

    interface ProfilePictureUploadDialogListener {
        fun onCameraUpload(dialog: DialogFragment)
        fun onGalleryUpload(dialog: DialogFragment)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val inflatedView = inflater.inflate(R.layout.profile_picture_dialog, container, false)
        dialog?.setCanceledOnTouchOutside(true)
        return inflatedView
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as ProfilePictureUploadDialogListener
        } catch (e: ClassCastException) {
            Log.d("UploadDialog", e.toString())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraUploadButton.setOnClickListener {
            listener.onCameraUpload(this)
            dismiss()
        }

        galleryUploadButton.setOnClickListener {
            listener.onGalleryUpload(this)
            dismiss()
        }

        dismissButton.setOnClickListener {
            Toast.makeText(context, "dismiss", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }
}
