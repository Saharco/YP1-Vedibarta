package com.technion.vedibarta.userProfile

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.NonNull
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.technion.vedibarta.POJOs.ChatMetadata
import com.technion.vedibarta.POJOs.Gender
import com.technion.vedibarta.R
import com.technion.vedibarta.fragments.ChatListFragmentDirections
import com.technion.vedibarta.main.MainActivity
import com.technion.vedibarta.utilities.VedibartaActivity
import kotlinx.android.synthetic.main.other_user_profile_dialog.*

class OtherUserProfilePopUp : DialogFragment() {
    private val chatMetadata: ChatMetadata by lazy { arguments?.getSerializable(CHAT_METADATA_KEY) as ChatMetadata }
    private var inflatedView: View? = null

    override fun onCreateView(
        @NonNull inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        inflatedView = inflater.inflate(R.layout.other_user_profile_dialog, container, false)
        dialog?.setCanceledOnTouchOutside(true)
        return inflatedView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        otherUserName.text = chatMetadata.partnerName
        otherUserName.bringToFront()

        if (chatMetadata.partnerPhotoUrl == null) {
            displayDefaultProfilePicture(otherUserProfile, chatMetadata.partnerGender)
            otherUserProfile.scaleX = 1.7f
            otherUserProfile.scaleY = 1.7f
        }
        else {
            Glide.with(requireContext()).asBitmap().load(chatMetadata.partnerPhotoUrl)
                .apply(
                    RequestOptions.overrideOf(
                        VedibartaActivity.dpToPx(requireContext().resources, 240f).toInt(),
                        VedibartaActivity.dpToPx(requireContext().resources, 240f).toInt()
                    )
                )
                .listener(object : RequestListener<Bitmap> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        displayDefaultProfilePicture(
                            otherUserProfile,
                            chatMetadata.partnerGender
                        )
                        return false
                    }

                    override fun onResourceReady(
                        resource: Bitmap?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        otherUserProfile.setImageBitmap(resource)
                        return false
                    }
                })
                .into(otherUserProfile)
        }

        view.setOnClickListener {
            val action = ChatListFragmentDirections.actionChatsToUserProfileActivity(chatMetadata.partnerId)
            findNavController().navigate(action)
        }
    }

    private fun displayDefaultProfilePicture(v: ImageView, otherGender: Gender) {
        when (otherGender) {
            Gender.MALE -> v.setImageResource(R.drawable.ic_photo_default_profile_man)
            Gender.FEMALE -> v.setImageResource(R.drawable.ic_photo_default_profile_girl)
            else -> Log.d(MainActivity.TAG, "other student is neither male nor female??")
        }
    }

    companion object {
        private const val TAG = "Vedibarta/ProfileFrag"
        private const val CHAT_METADATA_KEY = "CHAT_METADATA_KEY"

        @JvmStatic
        fun newInstance(chatMetadata: ChatMetadata): OtherUserProfilePopUp {
            val f = OtherUserProfilePopUp()
            val args = Bundle()
            args.putSerializable(CHAT_METADATA_KEY, chatMetadata)
            f.arguments = args
            return f
        }
    }
}