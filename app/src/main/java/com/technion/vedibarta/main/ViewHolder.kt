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
import com.technion.vedibarta.utilities.ImageLoader
import com.technion.vedibarta.utilities.VedibartaActivity.Companion.database

class ViewHolder(val view: View, val userId: String, val context: Context) :
    RecyclerView.ViewHolder(view)
{
    private val imageLoader = ImageLoader()
    fun bind(chatMetadata: ChatMetadata)
    {
        itemView.findViewById<TextView>(R.id.user_name).text = chatMetadata.partnerName
        itemView.findViewById<TextView>(R.id.last_message).text = chatMetadata.lastMessage
        if (chatMetadata.numMessages != 0)
        {
            val t = database.clock.calcRelativeTime(chatMetadata.lastMessageTimestamp, context)
            itemView.findViewById<TextView>(R.id.relative_timestamp).text = t
        }
        imageLoader.loadProfileImage(chatMetadata.partnerPhotoUrl,
                                     chatMetadata.partnerGender,
                                     itemView.findViewById(R.id.user_picture),
                                     context)

    }
}