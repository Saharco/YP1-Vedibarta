package com.technion.vedibarta.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.Rect
import android.graphics.Typeface
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.facebook.login.LoginManager
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.technion.vedibarta.POJOs.Gender
import com.technion.vedibarta.R
import com.technion.vedibarta.main.MainActivity
import com.technion.vedibarta.userProfile.ProfilePictureUploadDialog
import com.technion.vedibarta.utilities.RotateBitmap
import com.technion.vedibarta.utilities.VedibartaActivity
import com.technion.vedibarta.utilities.VedibartaFragment
import com.technion.vedibarta.utilities.resourcesManagement.*
import kotlinx.android.synthetic.main.fragment_user_profile.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

class UserProfileFragment : Fragment(), MainActivity.OnBackPressed{
    private val TAG = "user-profile"

    private val REQUEST_CAMERA = 1
    private val SELECT_IMAGE = 2
    private val EDIT_PROFILE = 3

    //TODO: fetch this information from User class!!! change this to null later
    private var userPhotoURL: String? = VedibartaActivity.student!!.photo

    private var mCurrentAnimator: Animator? = null
    private var mShortAnimationDuration: Long? = null
    private var isImageFullscreen = false

    private var minimizer: View.OnClickListener? = null

    private lateinit var characteristicsTask: Task<MultilingualTextResource>
    private lateinit var hobbiesTask: Task<MultilingualTextResource>
    private lateinit var hobbiesImagesTask: Task<Map<String, File>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "created UserProfileActivity")
        characteristicsTask = RemoteTextResourcesManager(requireContext())
            .findMultilingualResource("characteristics/all")
        hobbiesTask = RemoteTextResourcesManager(requireContext())
            .findMultilingualResource("hobbies/all")
        hobbiesImagesTask = RemoteFileResourcesManager(requireContext())
            .getAllInDirectory("images/hobbies")
    }

    override fun onStart() {
        super.onStart()
        resetTables()
        loadUserData()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_profile, container, false)
    }

    private fun resetTables() {
        characteristicsTable.removeAllViews()
        hobbiesTable.removeAllViews()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            initWidgets()
            toolbar.inflateMenu(R.menu.user_profile_menu)
            toolbar.setOnMenuItemClickListener {
                onMenuItemClick(it)
            }
        }
    }

    private fun onMenuItemClick(it: MenuItem): Boolean {
        when (it.itemId) {
            R.id.actionLogOut -> onLogoutClick()
            R.id.actionEditProfile -> findNavController().navigate(R.id.action_user_profile_to_profileEditActivity)
        }

        return true
    }

    private fun onLogoutClick() {
        val title = TextView(requireContext())
        title.setText(R.string.dialog_logout_title)
        title.textSize = 20f
        title.setTypeface(null, Typeface.BOLD)
        title.setTextColor(ContextCompat.getColor(requireContext(), R.color.textPrimary))
        title.gravity = Gravity.CENTER
        title.setPadding(10, 40, 10, 24)

        var msg = R.string.dialog_logout_message_m
        if (VedibartaActivity.student!!.gender == Gender.FEMALE)
            msg = R.string.dialog_logout_message_f

        val builder = AlertDialog.Builder(requireContext())
        builder.setCustomTitle(title)
            .setMessage(msg)
            .setPositiveButton(R.string.yes) { _, _ ->
                performLogout()
            }
            .setNegativeButton(R.string.no) { _, _ -> }
            .show()
        builder.create()
    }

    private fun performLogout() {
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener {
            if (!it.isSuccessful) {
                Log.d(MainActivity.TAG, "logout failed")
                return@addOnCompleteListener
            }
            val token = it.result?.token
            Log.d(MainActivity.TAG, "token is: $token")
            VedibartaActivity.database.students().userId().build()
                .update("tokens", FieldValue.arrayRemove(token))

            FirebaseAuth.getInstance().signOut()
            LoginManager.getInstance().logOut()
            findNavController().navigate(R.id.action_user_profile_to_loginActivity)
        }
    }

    override fun onBackPressed(): Boolean {
        if (isImageFullscreen && !minimizeFullscreenImage()) {
            Log.d(TAG, "onBackPressed: closing fullscreen image")
            return false
        }
        return true
    }


    private fun initWidgets() {
        enlargedToolbar.title = VedibartaActivity.student!!.name
        enlargedToolbar.setTitleTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
        titlePicture.bringToFront()
        profilePicture.bringToFront()
        changeProfilePictureButton.bringToFront()

        changeProfilePictureButton.setOnClickListener {
            ProfilePictureUploadDialog.newInstance(
                VedibartaActivity.student!!.name,
                VedibartaActivity.student!!.gender
            ).show(parentFragmentManager,
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
        Tasks.whenAll(hobbiesTask, characteristicsTask, hobbiesImagesTask)
            .addOnSuccessListener(requireActivity()) {
                val studentCharacteristics = characteristicsTask.result!!.toCurrentLanguage(
                    VedibartaActivity.student!!.characteristics.keys.toTypedArray()
                )

                VedibartaFragment.populateCharacteristicsTable(
                    requireContext(),
                    characteristicsTable,
                    studentCharacteristics,
                    VedibartaActivity.student!!.characteristics.keys.toMutableList(),
                    characteristicsTask.result!!
                )
                characteristicsTable.forEach { view ->
                    (view as TableRow).forEach { v ->
                        v.isClickable = false
                    }
                }

                val hobbies =
                    hobbiesTask.result!!.toCurrentLanguage(VedibartaActivity.student!!.hobbies.toTypedArray())

                VedibartaFragment.populateHobbiesTable(
                    requireContext(),
                    hobbiesTable,
                    hobbies,
                    VedibartaActivity.student!!.hobbies.toMutableList(),
                    hobbiesTask.result!!,
                    hobbiesImagesTask.result!!
                )
                hobbiesTable.forEach { view ->
                    (view as TableRow).forEach { v ->
                        v.isClickable = false
                    }
                }
            }

        populateProfilePicture()
        populateUsername()
        populateUserRegion()
    }

    private fun populateUserRegion() {
        userName.text = VedibartaActivity.student!!.name
    }

    private fun populateUsername() {
        userDescription.text = VedibartaActivity.student!!.region
    }

    private fun populateProfilePicture() {
        if (VedibartaActivity.student!!.photo == null) {
            loadDefaultUserProfilePicture()
            return
        }

        Glide.with(requireActivity().applicationContext)
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


        Glide.with(requireActivity().applicationContext)
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
        if (VedibartaActivity.student!!.gender == Gender.MALE)
            profilePicture.setImageResource(R.drawable.ic_photo_default_profile_man)
        else
            profilePicture.setImageResource(R.drawable.ic_photo_default_profile_girl)
        displayUserProfilePicture()
    }

    private fun setToolbar(tb: Toolbar) {
        (activity as AppCompatActivity).setSupportActionBar(tb)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    private fun toggleToolbars() {
        if (!isImageFullscreen) {
            Log.d(TAG, "toggleToolbars: setting the real toolbar")
            enlargedToolbar.visibility = View.GONE
            toolbar.visibility = View.VISIBLE
            toolbar.inflateMenu(R.menu.user_profile_menu)
            toolbar.setOnMenuItemClickListener {
                onMenuItemClick(it)
            }

            VedibartaActivity.changeStatusBarColor(
                requireActivity(),
                ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark)
            )
        } else {
            Log.d(TAG, "toggleToolbars: setting the fake toolbar")
            toolbar.visibility = View.GONE
            enlargedToolbar.visibility = View.VISIBLE
            setToolbar(enlargedToolbar)
            (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(true)
            (activity as AppCompatActivity).supportActionBar?.title =
                VedibartaActivity.student!!.name
            VedibartaActivity.changeStatusBarColor(
                requireActivity(),
                ContextCompat.getColor(requireContext(), android.R.color.black)
            )
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

        Glide.with(requireActivity().applicationContext)
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

        root.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.white))
        scrollViewLayout.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                android.R.color.white
            )
        )

        mCurrentAnimator = null
    }

    private fun hideScreenElements() {
        fullscreenImage.visibility = View.VISIBLE
        fullscreenImageContainer.visibility = View.VISIBLE

        root.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.black))
        scrollViewLayout.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                android.R.color.black
            )
        )

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


    fun checkImageAndUpload(image: Uri?) {
        if (image != null) {
            if (!validateImage(image)) {
                Toast.makeText(
                    requireContext(),
                    R.string.image_not_allowed_message,
                    Toast.LENGTH_LONG
                ).show()
            } else if (!textValidate(image)) {
                Toast.makeText(
                    requireContext(),
                    R.string.texts_not_allowed_message,
                    Toast.LENGTH_LONG
                ).show()
            } else {
                uploadPhoto(image)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "got result from upload activity")

        if (resultCode == Activity.RESULT_OK) {

            when (requestCode) {
                REQUEST_CAMERA -> checkImageAndUpload((activity as MainActivity).selectedImage)
                SELECT_IMAGE -> checkImageAndUpload(data!!.data)
//                EDIT_PROFILE -> {
//                    val snackbar = Snackbar.make(
//                        toolbar,
//                        resources.getString(R.string.edit_changes_saved_successfully),
//                        Snackbar.LENGTH_LONG
//                    )
//                        .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.colorAccentDark))
//                    snackbar.view.layoutDirection = View.LAYOUT_DIRECTION_RTL
//                    snackbar.show()
//                }
            }
        }
    }

    private fun validateImage(imageUriForVision: Uri): Boolean {
        try {
            val image = FirebaseVisionImage.fromFilePath(
                requireActivity().applicationContext,
                imageUriForVision
            )
            val labeler = FirebaseVision.getInstance().onDeviceImageLabeler.processImage(image)

            while (!labeler.isComplete) {
            }
            Log.d(TAG, "${labeler.result?.toString()}")
            if (labeler.isSuccessful) {
                val labels = labeler.result!!.map { it.text }
                val res = labels.intersect(listOf("Skin", "Swimwear")).isEmpty()
                Log.d("wtf", "Image validation result: $res")
                return res
            }
        } catch (e: Exception) {
            Log.d(TAG, "validateImage ${e.message}, cause: ${e.cause?.message}")
        }

        return false
    }

    private fun textValidate(imageUriForVision: Uri): Boolean {
        val image = FirebaseVisionImage.fromFilePath(
            requireActivity().applicationContext,
            imageUriForVision
        )
        val detector = FirebaseVision.getInstance().onDeviceTextRecognizer.processImage(image)

        while (!detector.isComplete) {
        }
        if (detector.isSuccessful) {
            Log.d("wtf", "Text Detector result: ${detector.result!!.text.isEmpty()}")
            return detector.result!!.text.isEmpty()
        }
        return false
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
        val storageRef = (activity as MainActivity).storage.students().userId().pictures()
            .fileName("profile_pic")
        startLoadingPictureChange()
        storageRef.putBytes(bytes)
            .addOnSuccessListener {
                storageRef.downloadUrl
                    .addOnSuccessListener {
                        userPhotoURL = it.toString()
                        VedibartaActivity.database.students().userId().build()
                            .set(mapOf(Pair("photo", userPhotoURL)), SetOptions.merge())
                            .addOnSuccessListener {
                                Log.d(TAG, "successfully updated user profile picture")
                                VedibartaActivity.student!!.photo = userPhotoURL
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
                    rotateBitmap.handleSamplingAndRotationBitmap(requireContext(), uris[0])
                Log.d(
                    TAG,
                    "doInBackground: MBs before compression: " + bitmap!!.byteCount.toDouble() / 1e6
                )
                val bytes = getBytesFromBitmap(
                    bitmap,
                    VedibartaActivity.IMAGE_COMPRESSION_QUALITY_IN_PERCENTS
                )
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
