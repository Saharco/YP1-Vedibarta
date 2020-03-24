package com.technion.vedibarta.main

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.technion.vedibarta.POJOs.Chat
import com.technion.vedibarta.POJOs.ChatMetadata
import com.technion.vedibarta.POJOs.Gender
import com.technion.vedibarta.POJOs.Student
import com.technion.vedibarta.R
import com.technion.vedibarta.chatRoom.ChatRoomActivity
import com.technion.vedibarta.database.DatabaseVersioning
import com.technion.vedibarta.utilities.VedibartaActivity
import java.util.HashMap

/***
 * base class adapter for adapters which are used by the RecyclerView of chat history
 * all adapter used in MainActivity should extend this
 */
abstract class MainAdapter(private val recycler: RecyclerView): RecyclerView.Adapter<RecyclerView.ViewHolder>()
{
    open fun startListening()
    {
        recycler.adapter = this
    }

    open fun stopListening()
    {
        recycler.adapter = null
    }
}