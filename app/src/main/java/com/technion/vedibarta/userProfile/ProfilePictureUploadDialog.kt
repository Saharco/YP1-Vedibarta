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
import com.technion.vedibarta.POJOs.Gender
import kotlinx.android.synthetic.main.profile_picture_dialog.*

class ProfilePictureUploadDialog private constructor() : DialogFragment() {

    private lateinit var listener: ProfilePictureUploadDialogListener
    private lateinit var userName: String
    private lateinit var gender: Gender


    interface ProfilePictureUploadDialogListener {
        fun onCameraUploadClicked(dialog: DialogFragment)
        fun onGalleryUploadClicked(dialog: DialogFragment)
    }

    companion object {
        fun newInstance(name: String, gender: Gender): ProfilePictureUploadDialog {
            val fragment = ProfilePictureUploadDialog()
            val args = Bundle()
            args.putString("name", name.substringBefore(' '))
            args.putSerializable("gender", gender)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userName = arguments!!.getString("name")!!
        gender = arguments!!.getSerializable("gender")!! as Gender

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
        if(gender != Gender.FEMALE)
            alertTitle.text = "$userName${alertTitle.text}"
        else
            alertTitle.text = "$userName${R.string.user_profile_dialog_title_suffix_f}"


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
