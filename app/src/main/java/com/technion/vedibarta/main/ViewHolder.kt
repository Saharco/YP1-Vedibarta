package com.technion.vedibarta.main

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.technion.vedibarta.POJOs.ChatMetadata
import com.technion.vedibarta.POJOs.Gender
import com.technion.vedibarta.R
import com.technion.vedibarta.userProfile.OtherUserProfilePopUp
import com.technion.vedibarta.utilities.VedibartaActivity.Companion.database

class ViewHolder(val view: View, val userId: String, val context: Context) :
    RecyclerView.ViewHolder(view) {
    fun bind(chatMetadata: ChatMetadata) {
        itemView.findViewById<TextView>(R.id.user_name).text = chatMetadata.partnerName
        itemView.findViewById<TextView>(R.id.last_message).text = chatMetadata.lastMessage
        if (chatMetadata.numMessages != 0) {
            val t = database.calcRelativeTime(chatMetadata.lastMessageTimestamp, context)
            itemView.findViewById<TextView>(R.id.relative_timestamp).text = t
        }

        val profilePicture = itemView.findViewById<ImageView>(R.id.user_picture)

        if (chatMetadata.partnerPhotoUrl == null)
            displayDefaultProfilePicture(profilePicture, chatMetadata.partnerGender)
        else {

            Glide.with(context).asBitmap().load(chatMetadata.partnerPhotoUrl)
                .apply(RequestOptions.circleCropTransform())
                .listener(object : RequestListener<Bitmap> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Bitmap>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        displayDefaultProfilePicture(
                            profilePicture,
                            chatMetadata.partnerGender
                        )
                        return false
                    }

                    override fun onResourceReady(
                        resource: Bitmap?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Bitmap>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        profilePicture.setImageBitmap(resource)
                        return false
                    }

                }).into(profilePicture)
        }
        setProfilePictureDialog(profilePicture, chatMetadata)
    }

    private fun setProfilePictureDialog(
        profilePicture: ImageView,
        chatMetadata: ChatMetadata
    ) {
        profilePicture.setOnClickListener {
            OtherUserProfilePopUp.newInstance(chatMetadata)
                .show((context as AppCompatActivity).supportFragmentManager, null)
        }
    }

    private fun displayDefaultProfilePicture(v: ImageView, otherGender: Gender) {
        when (otherGender) {
            Gender.MALE -> v.setImageResource(R.drawable.ic_photo_default_profile_man)
            Gender.FEMALE -> v.setImageResource(R.drawable.ic_photo_default_profile_girl)
            else -> Log.d(MainActivity.TAG, "other student is neither male nor female??")
        }
    }
}