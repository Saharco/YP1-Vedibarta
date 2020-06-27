package com.technion.vedibarta.userProfile

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import androidx.navigation.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.technion.vedibarta.POJOs.Gender
import com.technion.vedibarta.POJOs.Student
import com.technion.vedibarta.R
import com.technion.vedibarta.database.DatabaseVersioning
import com.technion.vedibarta.utilities.VedibartaActivity
import com.technion.vedibarta.utilities.VedibartaFragment
import com.technion.vedibarta.utilities.resourcesManagement.*
import kotlinx.android.synthetic.main.activity_user_profile.*
import java.io.File


class UserProfileActivity : VedibartaActivity() {

    private val TAG = "user-profile"
    val args: UserProfileActivityArgs by navArgs()

    //TODO: fetch this information from User class!!! change this to null later
    private lateinit var student: Student
    private var mCurrentAnimator: Animator? = null
    private var mShortAnimationDuration: Long? = null
    private var isImageFullscreen = false

    private var minimizer: View.OnClickListener? = null

    private lateinit var characteristicsTask: Task<MultilingualTextResource>
    private lateinit var hobbiesTask: Task<MultilingualTextResource>
    private lateinit var hobbiesImagesTask: Task<Map<String, File>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)
        DatabaseVersioning.currentVersion.instance.collection("students")
            .whereEqualTo("uid", args.userId).get()
            .addOnSuccessListener {
                student = it.documents.first().toObject(Student::class.java)!!
                initWidgets()
                resetTables()
                loadUserData()
            }
        Log.d(TAG, "created UserProfileActivity")
        characteristicsTask = RemoteTextResourcesManager(this)
            .findMultilingualResource("characteristics/all")
        hobbiesTask = RemoteTextResourcesManager(this)
            .findMultilingualResource("hobbies/all")
        hobbiesImagesTask = RemoteFileResourcesManager(this)
            .getAllInDirectory("images/hobbies")
    }

    private fun resetTables() {
        bubblesRecycleView.removeAllViews()
        hobbiesTable.removeAllViews()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "onOptionsItemSelected started")
        when (item.itemId) {
            android.R.id.home ->
                onBackPressed()
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


    private fun initWidgets() {
        setToolbar(toolbar)
        enlargedToolbar.title = student.name
        enlargedToolbar.setTitleTextColor(ContextCompat.getColor(this, android.R.color.white))

        titlePicture.bringToFront()
        profilePicture.bringToFront()

        profilePicture.setOnClickListener {
            Log.d(TAG, "clicked profile picture")
            zoomImageFromThumb(profilePicture)
        }
        mShortAnimationDuration =
            resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

    }

    private fun loadUserData() {
        Tasks.whenAll(hobbiesTask, characteristicsTask, hobbiesImagesTask)
            .addOnSuccessListener(this) {
                val studentCharacteristics =
                    characteristicsTask.result!!.toCurrentLanguage(student.characteristics.keys.toTypedArray())

                VedibartaFragment.populateCharacteristicsTable(
                    this,
                    bubblesRecycleView,
                    studentCharacteristics,
                    student!!.characteristics.keys.toMutableList(),
                    characteristicsTask.result!!
                )
                bubblesRecycleView.forEach { view ->
                    (view as TableRow).forEach { v ->
                        v.isClickable = false
                    }
                }

                val hobbies =
                    hobbiesTask.result!!.toCurrentLanguage(student.hobbies.toTypedArray())

                VedibartaFragment.populateHobbiesTable(
                    this,
                    hobbiesTable,
                    hobbies,
                    student!!.hobbies.toMutableList(),
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
        userName.text = student.name
    }

    private fun populateUsername() {
        userDescription.text = student.region
    }

    private fun populateProfilePicture() {
        if (student.photo == null) {
            loadDefaultUserProfilePicture()
            return
        }

        Glide.with(applicationContext)
            .asBitmap()
            .load(student.photo)
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
            .load(student.photo)
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
        profilePicturePB.visibility = View.GONE
    }

    private fun loadDefaultUserProfilePicture() {
        if (student.gender == Gender.MALE)
            profilePicture.setImageResource(R.drawable.ic_photo_default_profile_man)
        else
            profilePicture.setImageResource(R.drawable.ic_photo_default_profile_girl)
        displayUserProfilePicture()
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
            changeStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
        } else {
            Log.d(TAG, "toggleToolbars: setting the fake toolbar")
            toolbar.visibility = View.GONE
            enlargedToolbar.visibility = View.VISIBLE
            setToolbar(enlargedToolbar)
            supportActionBar?.setDisplayShowTitleEnabled(true)
            supportActionBar?.title = student.name
            changeStatusBarColor(ContextCompat.getColor(this, android.R.color.black))
        }
    }

    /**
     * Here be dragons.
     * This code is almost blindly copy-pasted and horribly designed
     */
    private fun zoomImageFromThumb(thumbView: View) {
        Log.d(TAG, "zoomImageFromThumb: Starting")

        if (student.photo == null) {
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
            .load(student.photo)
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

        root.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white))
        scrollViewLayout.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white))

        mCurrentAnimator = null
    }

    private fun hideScreenElements() {
        fullscreenImage.visibility = View.VISIBLE
        fullscreenImageContainer.visibility = View.VISIBLE

        root.setBackgroundColor(ContextCompat.getColor(this, android.R.color.black))
        scrollViewLayout.setBackgroundColor(ContextCompat.getColor(this, android.R.color.black))

        scrollViewLayout.visibility = View.GONE
        titlePicture.visibility = View.GONE
        userName.visibility = View.GONE
        userDescription.visibility = View.GONE
        divider1.visibility = View.GONE
    }

    private fun minimizeFullscreenImage(): Boolean {
        if (minimizer == null) {
            return false
        }

        fullscreenImage.resetZoom()
        minimizer?.onClick(fullscreenImage)
        return true
    }
}
