package com.technion.vedibarta.utilities

import android.view.View

class ListenersSetter {
    fun setListener(v: View, f: (View) -> Unit): ListenersSetter {
        val clickListener = View.OnClickListener { view ->
            f(view)
        }
        v.setOnClickListener(clickListener)

        return this
    }
}