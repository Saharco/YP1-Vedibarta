package com.technion.vedibarta.utilities

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.technion.vedibarta.POJOs.Gender
import com.technion.vedibarta.POJOs.User
import com.technion.vedibarta.R
import com.technion.vedibarta.main.MainActivity

class ImageLoader {

    fun loadProfileImage(user: User,view: ImageView, context: Context)
    {
        loadProfileImage(user.photo, user.gender, view, context)
    }

    fun loadProfileImage(photoUrl: String?, gender:Gender, view: ImageView, context: Context)
    {
        val profilePicture = view

        if (photoUrl == null)
            displayDefaultProfilePicture(profilePicture, gender)
        else
        {
            Glide.with(context).asBitmap().load(photoUrl)
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
                            gender
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
    }


    private fun displayDefaultProfilePicture(v: ImageView, otherGender: Gender) {
        when (otherGender) {
            Gender.MALE -> v.setImageResource(R.drawable.ic_photo_default_profile_man)
            Gender.FEMALE -> v.setImageResource(R.drawable.ic_photo_default_profile_girl)
            else -> Log.d(MainActivity.TAG, "other student is neither male nor female??")
        }

    }
}