package com.technion.vedibarta.main

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.technion.vedibarta.POJOs.ChatMetadata
import com.technion.vedibarta.R
import com.technion.vedibarta.chatRoom.ChatRoomActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.util.HashMap

/***
 * base class adapter for adapters which are used to filter the [RecyclerView] of chat history
 * every adapter that is used to filter in [MainActivity] should extend this
 */
abstract class MainsSearchAdapter<in T>(recycler: RecyclerView): MainAdapter(recycler)
{
    abstract fun filter(query: T)
}
