package com.technion.vedibarta.teacher

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
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.SetOptions
import com.technion.vedibarta.POJOs.Gender
import com.technion.vedibarta.POJOs.Grade
import com.technion.vedibarta.POJOs.Teacher
import com.technion.vedibarta.R
import com.technion.vedibarta.adapters.BubblesSelectionAdapter
import com.technion.vedibarta.adapters.ScheduleAdapter
import com.technion.vedibarta.data.TeacherMeta
import com.technion.vedibarta.data.TeacherResources
import com.technion.vedibarta.data.viewModels.BubbleViewModel
import com.technion.vedibarta.databinding.FragmentTeacherProfileBinding
import com.technion.vedibarta.main.MainActivity
import com.technion.vedibarta.userProfile.ProfilePictureUploadDialog
import com.technion.vedibarta.utilities.RotateBitmap
import com.technion.vedibarta.utilities.VedibartaActivity
import com.technion.vedibarta.utilities.logout
import com.technion.vedibarta.utilities.resourcesManagement.toCurrentLanguage
import kotlinx.android.synthetic.main.fragment_teacher_schedule.*
import kotlinx.android.synthetic.main.fragment_user_profile.*
import java.io.ByteArrayOutputStream
import java.io.IOException

class TeacherProfileFragment : Fragment() {

    private var mCurrentAnimator: Animator? = null
    private var mShortAnimationDuration: Long? = null
    private var isImageFullscreen = false
    private var minimizer: View.OnClickListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentTeacherProfileBinding.inflate(inflater, container, false)
        populateWithData(binding)
        binding.actionLogOut.setOnClickListener {
            onLogoutClick()
        }
        binding.actionEditProfile.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_teacherProfileEditFragment)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        changeProfilePictureButton.bringToFront()

        changeProfilePictureButton.setOnClickListener {
            ProfilePictureUploadDialog.newInstance(
               TeacherMeta.teacher.name,
                TeacherMeta.teacher.gender
            ).show(
                parentFragmentManager,
                "UploadProfilePictureFragment"
            )
        }

//        profilePicture.setOnClickListener {
//            zoomImageFromThumb(profilePicture)
//        }
        mShortAnimationDuration =
            resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
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
        //if (VedibartaActivity.student!!.gender == Gender.FEMALE)
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
        logout().addOnCompleteListener(requireActivity()) {
            if (it.result!!) {
                findNavController().navigate(R.id.action_teacher_profile_to_loginActivity)
                requireActivity().finish()
            } else {
                Toast.makeText(
                    requireContext(),
                    R.string.something_went_wrong,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun populateRecyclerView(recyclerView: RecyclerView, dataList: List<String>) {
        val bubblesViewModels = dataList.map {
            val marked = MutableLiveData(true)

            BubbleViewModel(
                MutableLiveData(it),
                marked
            )
        }
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        recyclerView.adapter = BubblesSelectionAdapter(viewLifecycleOwner, bubblesViewModels, false)

    }


    private fun populateWithData(binding: FragmentTeacherProfileBinding) {
        if (TeacherMeta.teacher.photo == null) {
            populateDefaultProfilePicture(binding)
        } else {
            Glide.with(requireContext().applicationContext)
                .asBitmap()
                .load(TeacherMeta.teacher.photo)
                .apply(RequestOptions.circleCropTransform())
                .into(binding.profilePicture)
        }

        binding.profilePicture.visibility = View.VISIBLE
        binding.profilePicturePB.visibility = View.INVISIBLE
        binding.userName.text = TeacherMeta.teacher.name
        var text = ""
        TeacherMeta.teacher.schools.zip(TeacherMeta.teacher.regions).forEach {
            text += "${it.first.substringBeforeLast(" -")}, ${it.second}\n"
        }
        binding.userDescription.text = "$text ${gradesToString()}"
        populateRecyclerView(
            binding.characteristicsRecyclerView,
            TeacherResources.schoolCharacteristics.translator.toCurrentLanguage(TeacherMeta.teacher.schoolCharacteristics.keys.toList())
                .toList()
        )
        populateRecyclerView(
            binding.subjectsRecyclerView,
            TeacherResources.subjects.translator.toCurrentLanguage(TeacherMeta.teacher.teachingSubjects.keys)
                .toList()
        )
        binding.schedule.isNestedScrollingEnabled = false
        binding.schedule.adapter = ScheduleAdapter(
            requireContext(),
            initialSchedule = TeacherMeta.teacher.getSchedule(),
            isEditable = false
        )
        val layoutManager = GridLayoutManager(requireContext(), 1 * 7 + 6 * 4)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int) = if (position % 7 == 0) 7 else 4
        }
        binding.schedule.layoutManager = layoutManager
        binding.schedule.layoutDirection = View.LAYOUT_DIRECTION_RTL
    }


    private fun gradesToString(): String {
        var text =
            if (TeacherMeta.teacher.gender == Gender.MALE) getString(R.string.grades_text_suffix_m) else getString(
                R.string.grades_text_suffix_f
            )
        text+= " "
        if (TeacherMeta.teacher.grades.tenth)
            text += "${getString(R.string.tenth)}, "
        if (TeacherMeta.teacher.grades.eleventh)
            text += "${getString(R.string.eleventh)}, "
        if (TeacherMeta.teacher.grades.twelfth)
            text += "${getString(R.string.twelfth)}"

        return text
    }

    private fun populateDefaultProfilePicture(binding: FragmentTeacherProfileBinding) {
        if (TeacherMeta.teacher.gender == Gender.MALE) {
            binding.profilePicture.setImageDrawable(
                getDrawable(
                    requireContext(),
                    R.drawable.ic_photo_default_profile_man
                )
            )
        } else {
            binding.profilePicture.setImageDrawable(
                getDrawable(
                    requireContext(),
                    R.drawable.ic_photo_default_profile_girl
                )
            )
        }
    }

    /**
     * Here be dragons.
     * This code is almost blindly copy-pasted and horribly designed
     */
    private fun zoomImageFromThumb(thumbView: View) {

        if (TeacherMeta.teacher.photo == null) {
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
            .load(TeacherMeta.teacher.photo)
            .into(object : SimpleTarget<Bitmap>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {
                    fullscreenImage.setImageBitmap(resource)
                }
            })

        isImageFullscreen = true
//        toggleToolbars()

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
//            toggleToolbars()
        }
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
        activity?.findViewById<BottomNavigationView>(R.id.bottomNavigationView)?.visibility =
            View.VISIBLE

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
        activity?.findViewById<BottomNavigationView>(R.id.bottomNavigationView)?.visibility =
            View.GONE
    }

    private fun minimizeFullscreenImage(): Boolean {
        if (minimizer == null) {
            return false
        }

        fullscreenImage.resetZoom()
        minimizer?.onClick(fullscreenImage)
        return true
    }

    fun uploadPhoto(imagePath: Uri) {
        val resize = ImageCompressTask()
        resize.execute(imagePath)
    }

    private fun updateServerUserProfilePic(bytes: ByteArray) {
        val storageRef = (activity as TeacherMainActivity).storage.teachers().userId().pictures()
            .fileName("profile_pic")
        startLoadingPictureChange()
        storageRef.putBytes(bytes)
            .addOnSuccessListener {
                storageRef.downloadUrl
                    .addOnSuccessListener {uri->
                        TeacherMeta.teacher.photo = uri.toString()
                        VedibartaActivity.database.teachers().user().build()
                            .set(mapOf(Pair("photo",  TeacherMeta.teacher.photo)), SetOptions.merge())
                            .addOnSuccessListener {
                                TeacherMeta.teacher.photo = uri.toString()
                                populateProfilePicture()
                            }
                            .addOnFailureListener {
                                finishLoadingPictureChange()
                            }
                    }
                    .addOnFailureListener {
                        finishLoadingPictureChange()
                    }
            }
            .addOnFailureListener {
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

    private fun populateProfilePicture() {
        if (TeacherMeta.teacher.photo == null) {
            loadDefaultUserProfilePicture()
            return
        }

        Glide.with(requireActivity().applicationContext)
            .asBitmap()
            .load(TeacherMeta.teacher.photo)
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
            .load(TeacherMeta.teacher.photo)
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

    private inner class ImageCompressTask : AsyncTask<Uri, Int, ByteArray>() {

        override fun onPreExecute() {
            startLoadingPictureChange()
        }

        override fun doInBackground(vararg uris: Uri): ByteArray? {
            try {
                val rotateBitmap = RotateBitmap()
                val bitmap =
                    rotateBitmap.handleSamplingAndRotationBitmap(requireContext(), uris[0])!!
                val bytes = getBytesFromBitmap(
                    bitmap,
                    VedibartaActivity.IMAGE_COMPRESSION_QUALITY_IN_PERCENTS
                )

                return bytes
            } catch (e: IOException) {
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
