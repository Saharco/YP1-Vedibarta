package com.technion.vedibarta.activities

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import kotlinx.android.synthetic.main.activity_user_profile.*
import android.view.LayoutInflater
import android.widget.*
import android.widget.TableLayout

import androidx.fragment.app.DialogFragment
import com.technion.vedibarta.R
import com.technion.vedibarta.views.ProfilePictureUploadDialog
import androidx.core.content.FileProvider
import android.net.Uri
import android.util.Log
import java.io.File
import android.app.Activity

import android.graphics.Bitmap
import android.os.AsyncTask
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.technion.vedibarta.utilities.RotateBitmap
import java.io.ByteArrayOutputStream
import java.io.IOException


class UserProfileActivity : AppCompatActivity(),
    ProfilePictureUploadDialog.ProfilePictureUploadDialogListener {

    private val REQUEST_CAMERA = 1
    private val SELECT_IMAGE = 2

    private var selectedImageFile: File? = null
    private var selectedImage: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)
        initWidgets()
    }

    private fun initWidgets() {
        initToolbar()
        populateTable()

        titlePicture.bringToFront()
        profilePicture.bringToFront()

        profilePicture.setOnClickListener {
            ProfilePictureUploadDialog().show(
                supportFragmentManager,
                "UploadProfilePictureFragment"
            )
        }
    }

    @SuppressLint("InflateParams")
    private fun populateTable() {

        //TODO: when the data is fetched from the database, change this dummy list
        val characteristics = listOf(
            "זמני1", "זמני2", "זמני3", "זמני4", "זמני5", "זמני6", "זמני7", "זמני8",
            "זמני9", "זמני10", "זמני11", "זמני12", "זמני13", "זמני14", "זמני15", "זמני16"
        )
        val characteristicsAmount = characteristics.size

        val tableRowParams = TableLayout.LayoutParams(
            TableLayout.LayoutParams.MATCH_PARENT,
            TableLayout.LayoutParams.WRAP_CONTENT
        )
        tableRowParams.setMargins(40, 40, 40, 40)

        val bubbleParams =
            TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )

        for (i in 0 until characteristicsAmount step 3) {
            val tableRow = TableRow(this)
            tableRow.layoutParams = tableRowParams
            tableRow.gravity = Gravity.CENTER_HORIZONTAL

            for (j in 0 until 3) {
                if (i + j >= characteristicsAmount)
                    break

                val bubbleFrame = LayoutInflater.from(this).inflate(
                    R.layout.user_profile_bubble,
                    null
                ) as FrameLayout

                val bubble = bubbleFrame.findViewById(R.id.invisibleBubble) as TextView
                bubble.text = characteristics[i + j]
                bubbleFrame.layoutParams = bubbleParams

                tableRow.addView(bubbleFrame)
            }

            characteristicsTable.addView(tableRow)
        }
    }

    private fun initToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onCameraUpload(dialog: DialogFragment) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        selectedImageFile = File(
            externalCacheDir,
            System.currentTimeMillis().toString() + ".jpg"
        )
        selectedImage = FileProvider.getUriForFile(
            this@UserProfileActivity,
            "$packageName.provider", selectedImageFile!!
        )
        intent.putExtra(MediaStore.EXTRA_OUTPUT, selectedImage)
        Log.d(this.localClassName, "Activating camera")
        startActivityForResult(intent, REQUEST_CAMERA)
    }

    override fun onGalleryUpload(dialog: DialogFragment) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, SELECT_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CAMERA -> if (selectedImage != null) {
                    uploadPhoto(selectedImage!!)
                }
                SELECT_IMAGE -> {
                    selectedImage = data!!.data
                    uploadPhoto(selectedImage!!)
                }
            }
        }
    }

    private fun uploadPhoto(imagePath: Uri) {
        val resize = ImageCompressTask()
        resize.execute(imagePath)
    }

    /**
     * Updates the user profile picture *IN THE DATABASE!*
     */
    private fun setUserProfilePic(bytes: ByteArray) {
    }

    private inner class ImageCompressTask : AsyncTask<Uri, Int, ByteArray>() {

        override fun onPreExecute() {
            profilePicturePB.visibility = View.VISIBLE
            profilePicture.visibility = View.INVISIBLE
        }

        override fun doInBackground(vararg uris: Uri): ByteArray? {
            try {
                val rotateBitmap = RotateBitmap()
                val bitmap =
                    rotateBitmap.handleSamplingAndRotationBitmap(this@UserProfileActivity, uris[0])
                Log.d(
                    "UserProfileActivity",
                    "doInBackground: MBs before compression: " + bitmap!!.byteCount.toDouble() / 1e6
                )
                val bytes = getBytesFromBitmap(bitmap, 80)
                Log.d(
                    "UserProfileActivity",
                    "doInBackground: MBs after compression: " + bytes.size.toDouble() / 1e6
                )
                return bytes
            } catch (e: IOException) {
                Log.d("UserProfileActivity", "doInBackground: exception: $e")
                return null
            }

        }

        override fun onPostExecute(bytes: ByteArray) {
            super.onPostExecute(bytes)
            Glide.with(applicationContext)
                .asBitmap()
                .load(bytes)
                .apply(RequestOptions.circleCropTransform())
                .into(profilePicture)
            profilePicturePB.visibility = View.GONE
            profilePicture.visibility = View.VISIBLE
            setUserProfilePic(bytes)
        }

        private fun getBytesFromBitmap(bitmap: Bitmap, quality: Int): ByteArray {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            return stream.toByteArray()
        }
    }
}
