package com.technion.vedibarta.main

import androidx.recyclerview.widget.RecyclerView

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