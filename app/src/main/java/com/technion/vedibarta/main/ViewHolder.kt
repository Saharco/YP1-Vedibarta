package com.technion.vedibarta.main

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.technion.vedibarta.POJOs.Chat
import com.technion.vedibarta.POJOs.ChatMetadata
import com.technion.vedibarta.POJOs.Gender
import com.technion.vedibarta.R
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class ViewHolder(val view: View, val userId: String, val context: Context) :
    RecyclerView.ViewHolder(view) {
    private fun getString(x: Int): String {
        return context.resources.getString(x)
    }

    private fun calcRelativeTime(time: Date): String
    {
        val current = Date(System.currentTimeMillis())
        val timeGap = current.time - time.time
        return when (val hoursGap = TimeUnit.HOURS.convert(timeGap, TimeUnit.MILLISECONDS))
        {
            in 0..1 -> getString(R.string.just_now)
            in 2..24 -> "${getString(R.string.sent)} ${getString(R.string.before)} $hoursGap ${getString(
                R.string.hours)}"
            in 24..48 -> "${getString(R.string.sent)} ${getString(R.string.yesterday)}"
            in 48..168 -> "${getString(R.string.sent)} ${getString(R.string.before)} ${hoursGap / 24} ${getString(
                R.string.days)}"
            else -> SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(time)
        }
    }

    fun bind(chatMetadata: ChatMetadata) {
        itemView.findViewById<TextView>(R.id.user_name).text = chatMetadata.partnerName
        itemView.findViewById<TextView>(R.id.last_message).text = chatMetadata.lastMessage
        itemView.findViewById<TextView>(R.id.relative_timestamp).text =
            calcRelativeTime(chatMetadata.lastMessageTimestamp)
        val profilePicture = itemView.findViewById<ImageView>(R.id.user_picture)

        if (chatMetadata.partnerPhotoUrl == null)
            displayDefaultProfilePicture(profilePicture, chatMetadata.partnerGender)
        else {

            Glide.with(context)
                .asBitmap()
                .load(chatMetadata.partnerPhotoUrl)
                .apply(RequestOptions.circleCropTransform())
                .listener(object : RequestListener<Bitmap> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Bitmap>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        displayDefaultProfilePicture(profilePicture, chatMetadata.partnerGender)
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

                })
                .into(profilePicture)
        }
    }

    fun bind(card: Chat, photoUrl: String? = null, otherGender: Gender = Gender.MALE) {
        try {
            val partnerId = card.getPartnerId(userId)
            itemView.findViewById<TextView>(R.id.user_name).text = card.getName(partnerId)
            itemView.findViewById<TextView>(R.id.last_message).text = card.lastMessage
            itemView.findViewById<TextView>(R.id.relative_timestamp).text =
                calcRelativeTime(card.lastMessageTimestamp)
            val profilePicture = itemView.findViewById<ImageView>(R.id.user_picture)

            if (photoUrl == null)
                displayDefaultProfilePicture(profilePicture, otherGender)
            else {

                Glide.with(context)
                    .asBitmap()
                    .load(photoUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .listener(object : RequestListener<Bitmap> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: com.bumptech.glide.request.target.Target<Bitmap>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            displayDefaultProfilePicture(profilePicture, otherGender)
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

                    })
                    .into(profilePicture)
            }

        } catch (e: Exception) {
            com.technion.vedibarta.utilities.error(e)
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