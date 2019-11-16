package com.technion.vedibarta.chatRoom

import android.app.Activity
import android.content.Context
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import com.technion.vedibarta.R

class ChatRoomListeners(private val context: Context) {
    fun configureListeners() {
        val popupMenu = (context as Activity).findViewById<View>(R.id.popupMenu)
        setListener(popupMenu) { v: View -> showPopup(v) }
    }

    private fun setListener(v: View, f: (View) -> Unit) {
        val clickListener = View.OnClickListener { view ->
            f(view)
        }
        v.setOnClickListener(clickListener)
    }

    private fun showPopup(view: View) {
        val popup = PopupMenu(context, view)
        popup.inflate(R.menu.chat_room_popup_menu)

        popup.setOnMenuItemClickListener { item: MenuItem? ->

            when (item!!.itemId) {
                R.id.generateQuestion -> {
                    Toast.makeText(context, item.title, Toast.LENGTH_SHORT).show()
                }
                R.id.reportAbuse -> {
                    Toast.makeText(context, item.title, Toast.LENGTH_SHORT).show()
                }
            }

            true
        }
        popup.show()
    }

}