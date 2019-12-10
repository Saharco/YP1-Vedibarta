package com.technion.vedibarta.userProfile

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import kotlinx.android.synthetic.main.activity_user_profile.*
import android.widget.*
import android.widget.TableLayout

import androidx.fragment.app.DialogFragment
import com.technion.vedibarta.R
import androidx.core.content.FileProvider
import android.net.Uri
import android.util.Log
import java.io.File
import android.app.Activity
import android.content.pm.PackageManager
import android.content.res.Resources

import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.Rect
import android.graphics.Typeface
import android.os.AsyncTask
import android.view.*
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.iid.FirebaseInstanceId
import com.technion.vedibarta.login.LoginActivity
import com.technion.vedibarta.utilities.Gender
import com.technion.vedibarta.utilities.RotateBitmap
import com.technion.vedibarta.utilities.VedibartaActivity
import com.technion.vedibarta.utilities.*
import kotlinx.android.synthetic.main.chat_card.*
import java.io.ByteArrayOutputStream
import java.io.IOException


class UserProfileActivity : VedibartaActivity(),
    ProfilePictureUploadDialog.ProfilePictureUploadDialogListener {

    private val TAG = "user-profile"

    private val APP_PERMISSION_REQUEST_CAMERA = 100
    private val REQUEST_CAMERA = 1
    private val SELECT_IMAGE = 2

    private var selectedImageFile: File? = null
    private var selectedImage: Uri? = null

    //TODO: fetch this information from User class!!! change this to null later
    private var userPhotoURL: String? = student!!.photo

    private var mCurrentAnimator: Animator? = null
    private var mShortAnimationDuration: Long? = null
    private var isImageFullscreen = false

    private var minimizer: View.OnClickListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)
        Log.d(TAG, "created UserProfileActivity")
        initWidgets()
    }

    override fun onStart() {
        super.onStart()
        resetTables()
        loadUserData()
    }

    private fun resetTables() {
        characteristicsTable.removeAllViews()
        hobbiesTable.removeAllViews()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.user_profile_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "onOptionsItemSelected started")
        when (item.itemId) {
            android.R.id.home ->
                if (isImageFullscreen) {
                    Log.d(TAG, "onOptionsItemSelected: fake toolbar clicked")
                    if (!minimizeFullscreenImage()) {
                        super.onBackPressed()
                    }
                } else {
                    Log.d(TAG, "onOptionsItemSelected: real toolbar clicked")
                    super.onBackPressed()
                }
            R.id.actionEditProfile ->
                startActivity(Intent(this, ProfileEditActivity::class.java))
            R.id.actionLogOut ->
                onLogoutClick()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (isImageFullscreen) {
            Log.d(TAG, "onBackPressed: closing fullscreen image")
            if (!minimizeFullscreenImage()) {
                super.onBackPressed()
            }
        } else {
            Log.d(TAG, "onBackPressed: finishing activity")
            super.onBackPressed()
        }
    }

    private fun onLogoutClick() {
        val title = TextView(this)
        title.setText(R.string.dialog_logout_title)
        title.textSize = 20f
        title.setTypeface(null, Typeface.BOLD)
        title.setTextColor(resources.getColor(R.color.textPrimary))
        title.gravity = Gravity.CENTER
        title.setPadding(10, 40, 10, 24)

        var msg = R.string.dialog_logout_message_m
        if (student!!.gender == Gender.FEMALE)
            msg = R.string.dialog_logout_message_f

        val builder = AlertDialog.Builder(this)
        builder.setCustomTitle(title)
            .setMessage(msg)
            .setPositiveButton(android.R.string.yes) {_, _ ->
                performLogout()
            }
            .setNegativeButton(android.R.string.no) {_, _ -> }
            .show()
        builder.create()
    }

    private fun performLogout() {
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener {
            if (!it.isSuccessful) {
                Log.d(TAG, "logout failed")
                return@addOnCompleteListener
            }
            val token = it.result?.token
            Log.d(TAG, "token is: $token")
            database.students().userId().build().update("tokens", FieldValue.arrayRemove(token))

            FirebaseAuth.getInstance().signOut()
            LoginManager.getInstance().logOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    private fun initWidgets() {
        setToolbar(toolbar)
        enlargedToolbar.title = student!!.name
        enlargedToolbar.setTitleTextColor(resources.getColor(android.R.color.white))

        titlePicture.bringToFront()
        profilePicture.bringToFront()
        changeProfilePictureButton.bringToFront()

        changeProfilePictureButton.setOnClickListener {
            ProfilePictureUploadDialog.newInstance(student!!.name, student!!.gender).show(
                supportFragmentManager,
                "UploadProfilePictureFragment"
            )
        }

        profilePicture.setOnClickListener {
            Log.d(TAG, "clicked profile picture")
            zoomImageFromThumb(profilePicture)
        }
        mShortAnimationDuration =
            resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

    }

    private fun loadUserData() {
        populateCharacteristicsTable()
        populateHobbiesTable()
        populateProfilePicture()
        populateUsername()
        populateUserRegion()
    }

    private fun populateUserRegion() {
        userName.text = student!!.name
    }

    private fun populateUsername() {
        userDescription.text = student!!.region
    }

    private fun populateProfilePicture() {
        if (student!!.photo == null) {
            loadDefaultUserProfilePicture()
            return
        }

        Glide.with(applicationContext)
            .asBitmap()
            .load(userPhotoURL)
            .into(object : SimpleTarget<Bitmap>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {
                    fullscreenImage.setImageBitmap(resource)
                }
            })


        Glide.with(applicationContext)
            .asBitmap()
            .load(userPhotoURL)
            .apply(RequestOptions.circleCropTransform())
            .listener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    isFirstResource: Boolean
                ): Boolean {
                    loadDefaultUserProfilePicture()
                    return false
                }

                override fun onResourceReady(
                    resource: Bitmap?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    displayUserProfilePicture()
                    return false
                }

            })
            .into(profilePicture)
    }

    private fun displayUserProfilePicture() {
        profilePicture.visibility = View.VISIBLE
        changeProfilePictureButton.visibility = View.VISIBLE
        profilePicturePB.visibility = View.GONE
    }

    private fun loadDefaultUserProfilePicture() {
        if (student!!.gender == Gender.MALE)
            profilePicture.setImageResource(R.drawable.ic_photo_default_profile_man)
        else
            profilePicture.setImageResource(R.drawable.ic_photo_default_profile_girl)
        displayUserProfilePicture()
    }

    @SuppressLint("InflateParams")
    private fun populateCharacteristicsTable() {

        val tableRowParams = TableLayout.LayoutParams(
            TableLayout.LayoutParams.MATCH_PARENT,
            TableLayout.LayoutParams.WRAP_CONTENT
        )
        tableRowParams.topMargin = 40 // in pixels

        val bubbleParams =
            TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )

        if (student == null || student!!.characteristics.isEmpty()) {
            handleNoCharacteristics()
            return
        }

        Log.d(TAG, "Screen width (in pixels): ${Resources.getSystem().displayMetrics.widthPixels}")

        val steps = calculateBubblesInRow()

        Log.d(TAG, "Amount of bubbles in a row: $steps")

        (student!!.characteristics.indices step steps).forEach { i ->
            val tableRow = TableRow(this)
            tableRow.layoutParams = tableRowParams
            tableRow.gravity = Gravity.CENTER_HORIZONTAL

            for (j in 0 until steps) {
                if (i + j >= student!!.characteristics.size)
                    break

                val bubbleFrame = LayoutInflater.from(this).inflate(
                    R.layout.user_profile_bubble_blue,
                    null
                ) as FrameLayout

                val bubble = bubbleFrame.findViewById(R.id.invisibleBubble) as TextView
                bubble.text = student!!.characteristics[i + j]
                bubbleFrame.layoutParams = bubbleParams

                tableRow.addView(bubbleFrame)
            }

            characteristicsTable.addView(tableRow)
        }
    }

    private fun calculateBubblesInRow(): Int =
        ((Resources.getSystem().displayMetrics.widthPixels - 48f.dpToPx()) / (100f.dpToPx())).toInt()

    private fun handleNoCharacteristics() {
        //TODO: add some behavior for the scenario where the user has no characteristics
    }

    @SuppressLint("InflateParams")
    private fun populateHobbiesTable() {

        val tableRowParams = TableLayout.LayoutParams(
            TableLayout.LayoutParams.MATCH_PARENT,
            TableLayout.LayoutParams.WRAP_CONTENT
        )
        tableRowParams.topMargin = 40 // in pixels

        val bubbleParams =
            TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )

        if (student == null || student!!.hobbies.isEmpty()) {
            handleNoHobbies()
            return
        }

        val steps = calculateBubblesInRow()
        (student!!.hobbies.indices step steps).forEach { i ->
            val tableRow = TableRow(this)
            tableRow.layoutParams = tableRowParams
            tableRow.gravity = Gravity.CENTER_HORIZONTAL

            for (j in 0 until steps) {
                if (i + j >= student!!.hobbies.size)
                    break

                val bubbleFrame = LayoutInflater.from(this).inflate(
                    R.layout.user_profile_bubble_orange,
                    null
                ) as FrameLayout

                val bubble = bubbleFrame.findViewById(R.id.invisibleBubble) as TextView
                bubble.text = student!!.hobbies[i + j]
                bubbleFrame.layoutParams = bubbleParams

                tableRow.addView(bubbleFrame)
            }

            hobbiesTable.addView(tableRow)
        }
    }

    private fun handleNoHobbies() {
        //TODO: add some behavior for the scenario where the user has no characteristics
    }

    private fun setToolbar(tb: Toolbar) {
        setSupportActionBar(tb)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    private fun toggleToolbars() {
        if (!isImageFullscreen) {
            Log.d(TAG, "toggleToolbars: setting the real toolbar")
            enlargedToolbar.visibility = View.GONE
            toolbar.visibility = View.VISIBLE
            setToolbar(toolbar)
            supportActionBar?.setDisplayShowTitleEnabled(false)
            changeStatusBarColor(resources.getColor(R.color.colorPrimaryDark))
        } else {
            Log.d(TAG, "toggleToolbars: setting the fake toolbar")
            toolbar.visibility = View.GONE
            enlargedToolbar.visibility = View.VISIBLE
            setToolbar(enlargedToolbar)
            supportActionBar?.setDisplayShowTitleEnabled(true)
            supportActionBar?.title = student!!.name
            changeStatusBarColor(resources.getColor(android.R.color.black))
        }
    }

    /**
     * Here be dragons.
     * This code is almost blindly copy-pasted and horribly designed
     */
    private fun zoomImageFromThumb(thumbView: View) {
        Log.d(TAG, "zoomImageFromThumb: Starting")

        if (userPhotoURL == null) {
            return
        }


        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator!!.cancel()
        }


        hideScreenElements()

        Glide.with(applicationContext)
            .asBitmap()
            .load(userPhotoURL)
            .into(object : SimpleTarget<Bitmap>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {
                    fullscreenImage.setImageBitmap(resource)
                }
            })

        isImageFullscreen = true
        toggleToolbars()

        Log.d(TAG, "zoomImageFromThumb: Inflated fullscreen image")

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        val startBounds = Rect()
        val finalBounds = Rect()
        val globalOffset = Point()

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds)
        fullscreenImageContainer.getGlobalVisibleRect(finalBounds, globalOffset)
        startBounds.offset(-globalOffset.x, -globalOffset.y)
        finalBounds.offset(-globalOffset.x, -globalOffset.y)

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        val startScale: Float?
        if (finalBounds.width().toFloat() / finalBounds.height()
            > startBounds.width().toFloat() / startBounds.height()
        ) {
            // Extend start bounds horizontally
            startScale = startBounds.height().toFloat() / finalBounds.height()
            val startWidth = startScale * finalBounds.width()
            val deltaWidth = (startWidth - startBounds.width()) / 2
            startBounds.left -= deltaWidth.toInt()
            startBounds.right += deltaWidth.toInt()
        } else {
            // Extend start bounds vertically
            startScale = startBounds.width().toFloat() / finalBounds.width()
            val startHeight = startScale * finalBounds.height()
            val deltaHeight = (startHeight - startBounds.height()) / 2
            startBounds.top -= deltaHeight.toInt()
            startBounds.bottom += deltaHeight.toInt()
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.alpha = 0f
        fullscreenImage.visibility = View.VISIBLE

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        fullscreenImage.pivotX = 0f
        fullscreenImage.pivotY = 0f

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        val animationSet = AnimatorSet()
        animationSet
            .play(
                ObjectAnimator.ofFloat(
                    fullscreenImage, View.X,
                    startBounds.left.toFloat(), finalBounds.left.toFloat()
                )
            )
            .with(
                ObjectAnimator.ofFloat(
                    fullscreenImage, View.Y,
                    startBounds.top.toFloat(), finalBounds.top.toFloat()
                )
            )
            .with(
                ObjectAnimator.ofFloat(
                    fullscreenImage, View.SCALE_X,
                    startScale, 1f
                )
            )
            .with(
                ObjectAnimator.ofFloat(
                    fullscreenImage,
                    View.SCALE_Y, startScale, 1f
                )
            )
        animationSet.duration = mShortAnimationDuration!!
        animationSet.interpolator = DecelerateInterpolator()
        animationSet.addListener(object : AnimatorListenerAdapter() {

            override fun onAnimationCancel(animation: Animator?) {
                mCurrentAnimator = null
            }

            override fun onAnimationEnd(animation: Animator?) {
                mCurrentAnimator = null
            }
        })
        animationSet.start()
        mCurrentAnimator = animationSet

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        minimizer = View.OnClickListener {
            if (mCurrentAnimator != null) {
                mCurrentAnimator!!.cancel()
            }

            // Animate the four positioning/sizing properties in parallel,
            // back to their original values.
            val animationSet = AnimatorSet()
            animationSet.play(
                ObjectAnimator
                    .ofFloat(fullscreenImage, View.X, startBounds.left.toFloat())
            )
                .with(
                    ObjectAnimator
                        .ofFloat(
                            fullscreenImage,
                            View.Y, startBounds.top.toFloat()
                        )
                )
                .with(
                    ObjectAnimator
                        .ofFloat(
                            fullscreenImage,
                            View.SCALE_X, startScale
                        )
                )
                .with(
                    ObjectAnimator
                        .ofFloat(
                            fullscreenImage,
                            View.SCALE_Y, startScale
                        )
                )
            animationSet.duration = mShortAnimationDuration!!
            animationSet.interpolator = DecelerateInterpolator()
            animationSet.addListener(object : AnimatorListenerAdapter() {

                override fun onAnimationEnd(animation: Animator) {
                    thumbView.alpha = 1f
                    restoreScreenElements()
                }

                override fun onAnimationCancel(animation: Animator) {
                    thumbView.alpha = 1f
                    restoreScreenElements()
                }
            })
            animationSet.start()
            mCurrentAnimator = animationSet
            isImageFullscreen = false
            toggleToolbars()
        }
        Log.d(TAG, "finishing zoomImageFromThumb")
    }

    private fun restoreScreenElements() {
        fullscreenImage.visibility = View.GONE
        fullscreenImageContainer.visibility = View.GONE

        scrollViewLayout.visibility = View.VISIBLE
        titlePicture.visibility = View.VISIBLE
        userName.visibility = View.VISIBLE
        userDescription.visibility = View.VISIBLE
        divider1.visibility = View.VISIBLE
        changeProfilePictureButton.visibility = View.VISIBLE

        root.setBackgroundColor(resources.getColor(android.R.color.white))
        scrollViewLayout.setBackgroundColor(resources.getColor(android.R.color.white))

        mCurrentAnimator = null
    }

    private fun hideScreenElements() {
        fullscreenImage.visibility = View.VISIBLE
        fullscreenImageContainer.visibility = View.VISIBLE

        root.setBackgroundColor(resources.getColor(android.R.color.black))
        scrollViewLayout.setBackgroundColor(resources.getColor(android.R.color.black))

        scrollViewLayout.visibility = View.GONE
        titlePicture.visibility = View.GONE
        userName.visibility = View.GONE
        userDescription.visibility = View.GONE
        divider1.visibility = View.GONE
        changeProfilePictureButton.visibility = View.GONE
    }

    private fun minimizeFullscreenImage(): Boolean {
        if (minimizer == null) {
            return false
        }

        fullscreenImage.resetZoom()
        minimizer?.onClick(fullscreenImage)
        return true
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
            "$packageName.provider", selectedImageFile!!
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
     * Updates the user profile picture *IN THE DATABASE!*,
     * updates the local Student class after the server update
     * TODO: move to database abstraction if possible
     */
    private fun updateServerUserProfilePic(bytes: ByteArray) {
        val storageRef = storage.students().userId().pictures().fileName("profile_pic")
        startLoadingPictureChange()
        storageRef.putBytes(bytes)
            .addOnSuccessListener {
                storageRef.downloadUrl
                    .addOnSuccessListener {
                        userPhotoURL = it.toString()
                        database.students().userId().build()
                            .set(mapOf(Pair("photo", userPhotoURL)), SetOptions.merge())
                            .addOnSuccessListener {
                                Log.d(TAG, "successfully updated user profile picture")
                                student!!.photo = userPhotoURL
                                populateProfilePicture()
                            }
                            .addOnFailureListener {
                                Log.d(TAG, "failed to update user profile picture")
                                finishLoadingPictureChange()
                            }
                    }
                    .addOnFailureListener {
                        Log.d(TAG, "failed to update user profile picture")
                        finishLoadingPictureChange()
                    }
            }
            .addOnFailureListener {
                Log.d(TAG, "failed to update user profile picture")
                finishLoadingPictureChange()
            }
    }

    private fun finishLoadingPictureChange() {
        profilePicturePB.visibility = View.GONE
        profilePicture.visibility = View.VISIBLE
        changeProfilePictureButton.visibility = View.VISIBLE
    }

    private fun startLoadingPictureChange() {
        profilePicturePB.visibility = View.VISIBLE
        profilePicture.visibility = View.INVISIBLE
        changeProfilePictureButton.visibility = View.GONE
    }

    private inner class ImageCompressTask : AsyncTask<Uri, Int, ByteArray>() {

        override fun onPreExecute() {
            startLoadingPictureChange()
        }

        override fun doInBackground(vararg uris: Uri): ByteArray? {
            try {
                val rotateBitmap = RotateBitmap()
                val bitmap =
                    rotateBitmap.handleSamplingAndRotationBitmap(this@UserProfileActivity, uris[0])
                Log.d(
                    TAG,
                    "doInBackground: MBs before compression: " + bitmap!!.byteCount.toDouble() / 1e6
                )
                val bytes = getBytesFromBitmap(bitmap, IMAGE_COMPRESSION_QUALITY_IN_PERCENTS)
                Log.d(
                    TAG,
                    "doInBackground: MBs after compression: " + bytes.size.toDouble() / 1e6
                )
                return bytes
            } catch (e: IOException) {
                Log.d(TAG, "doInBackground: exception: $e")
                return null
            }

        }

        override fun onPostExecute(bytes: ByteArray) {
            super.onPostExecute(bytes)
            updateServerUserProfilePic(bytes)
        }

        private fun getBytesFromBitmap(bitmap: Bitmap, quality: Int): ByteArray {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            return stream.toByteArray()
        }
    }
}
