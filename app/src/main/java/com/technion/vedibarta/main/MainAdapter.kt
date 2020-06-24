package com.technion.vedibarta.main

import androidx.recyclerview.widget.RecyclerView

/***
 * base class adapter for adapters which are used by the RecyclerView of chat history
 * every adapter that is used in MainActivity should extend this
 */
abstract class MainAdapter(private val recycler: RecyclerView): RecyclerView.Adapter<RecyclerView.ViewHolder>()
{
    /***
     * starts adapter functionality such as updating UI, etc.
     */
    open fun startListening()
    {
        recycler.adapter = this
    }

    /***
     * stops adapter functionality such as updating UI, etc.
     */
    open fun stopListening()
    {
        recycler.adapter = null
    }
}