package com.technion.vedibarta.utilities

import android.content.Context
import android.view.View

class ListenersBuilder {
    fun addListener(v: View, f: (View) -> Unit): ListenersBuilder {
        val clickListener = View.OnClickListener { view ->
            f(view)
        }
        v.setOnClickListener(clickListener)

        return this
    }
}