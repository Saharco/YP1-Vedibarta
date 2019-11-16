package com.technion.vedibarta.userProfile

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

class ProfilePictureUploadDialog private constructor() : DialogFragment() {

    private lateinit var listener: ProfilePictureUploadDialogListener
    private lateinit var userName: String

    interface ProfilePictureUploadDialogListener {
        fun onCameraUploadClicked(dialog: DialogFragment)
        fun onGalleryUploadClicked(dialog: DialogFragment)
    }

    companion object {
        fun newInstance(name: String): ProfilePictureUploadDialog {
            val fragment = ProfilePictureUploadDialog()
            val args = Bundle()
            args.putString("name", name.substringBefore(' '))
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userName = arguments!!.getString("name")!!
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
        alertTitle.text = "$userName${alertTitle.text}"

        cameraUploadButton.setOnClickListener {
            listener.onCameraUploadClicked(this)
            dismiss()
        }

        galleryUploadButton.setOnClickListener {
            listener.onGalleryUploadClicked(this)
            dismiss()
        }

        dismissButton.setOnClickListener {
            Toast.makeText(context, "dismiss", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }
}
