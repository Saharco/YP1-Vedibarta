package com.technion.vedibarta.main

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import com.technion.vedibarta.R
import com.technion.vedibarta.data.viewModels.StudentClassViewModel
import com.technion.vedibarta.fragments.UserProfileFragment
import com.technion.vedibarta.userProfile.ProfilePictureUploadDialog
import com.technion.vedibarta.utilities.VedibartaActivity
import java.io.File

private const val APP_PERMISSION_REQUEST_CAMERA = 100
private const val REQUEST_CAMERA = 1
private const val SELECT_IMAGE = 2

/***
 * main screen of the app, contains the chat history/list
 */
class MainActivity : VedibartaActivity(),
    ProfilePictureUploadDialog.ProfilePictureUploadDialogListener {
    private val navController by lazy { findNavController(R.id.main_content) }
    private val bottomNavigation by lazy { findViewById<BottomNavigationView>(R.id.bottomNavigationView) }

    private var selectedImageFile: File? = null
    var selectedImage: Uri? = null

    companion object {
        const val TAG = "Vedibarta/chat-lobby"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Firebase.dynamicLinks
            .getDynamicLink(intent)
            .addOnSuccessListener(this) {pendingDynamicLinkData ->
                var deepLink: Uri? = null
                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.link
                }
                if (user != null &&
                    deepLink != null &&
                    deepLink.getBooleanQueryParameter("classID", false)) {
                    val classId = deepLink.getQueryParameter("classID")
                    val viewModel by viewModels<StudentClassViewModel>()
                    viewModel.addStudentToClass(classId)
                }
            }
        setContentView(R.layout.activity_main)
        bottomNavigation.setupWithNavController(navController)

    }

    override fun onBackPressed() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_content)
        val fragment = navHostFragment!!.childFragmentManager.fragments[0]
        (fragment as? OnBackPressed)?.onBackPressed()?.not()?.let {
            if (it)
                super.onBackPressed()
        }
    }



    override fun onCameraUploadClicked(dialog: DialogFragment) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                APP_PERMISSION_REQUEST_CAMERA
            )
        } else {
            startCameraActivity()
        }
    }

    private fun startCameraActivity() {
        Log.d(TAG, "entered startCameraActivity")
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        selectedImageFile = File(
            externalCacheDir,
            System.currentTimeMillis().toString() + ".jpg"
        )
        Log.d(TAG, "fetched image file: $selectedImageFile")
        selectedImage = FileProvider.getUriForFile(
            this,
            "${packageName}.provider", selectedImageFile!!
        )

        Log.d(TAG, "fetched selected image: $selectedImage")
        intent.putExtra(MediaStore.EXTRA_OUTPUT, selectedImage)
        Log.d(TAG, "Activating camera")
        startActivityForResult(intent, REQUEST_CAMERA)
    }

    override fun onGalleryUploadClicked(dialog: DialogFragment) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, SELECT_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "got result from upload activity")

        if (resultCode == Activity.RESULT_OK) {
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.main_content)
            val fragment = navHostFragment!!.childFragmentManager.fragments[0]
            when (requestCode) {
                REQUEST_CAMERA -> (fragment as? UserProfileFragment)?.checkImageAndUpload(selectedImage)
                SELECT_IMAGE -> (fragment as? UserProfileFragment)?.checkImageAndUpload(data!!.data)
            }
        }
    }

    interface OnBackPressed {
        fun onBackPressed(): Boolean
    }
}
