package com.technion.vedibarta.chatRoom

import android.app.Activity
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.FragmentManager
import com.technion.vedibarta.POJOs.Message
import com.technion.vedibarta.POJOs.MessageType
import com.technion.vedibarta.R
import com.technion.vedibarta.utilities.ListenersSetter
import com.technion.vedibarta.utilities.VedibartaActivity

class ChatRoomListeners(private val chatRoom: ChatRoomActivity,
                        private val chatPartnerID: String,
                        private val supportFragmentManager: FragmentManager)
{

    fun configureListeners() {
        val popupMenu = chatRoom.findViewById<View>(R.id.popupMenu)
        val sendButton = chatRoom.findViewById<View>(R.id.buttonChatBoxSend)
        val setter = ListenersSetter()
        setter.setListener(popupMenu, this::showPopup)
        setter.setListener(sendButton,this::sendMessage)
    }

    private fun showPopup(view: View) {
        val popup = PopupMenu(chatRoom, view)
        popup.inflate(R.menu.chat_room_popup_menu)

        popup.setOnMenuItemClickListener { item: MenuItem? ->

            when (item!!.itemId) {
                R.id.generateQuestion -> {
                        ChatRoomQuestionGeneratorDialog().show(
                            supportFragmentManager,
                            "QuestionGeneratorFragment"
                        )
                }
                R.id.reportAbuse -> {
                    ChatRoomAbuseReportDialog().show(
                        supportFragmentManager,
                        "ReportAbuseDialog"
                    )
                }
            }

            true
        }
        popup.show()
    }

    private fun sendMessage(v: View)
    {
        val chatBox: EditText = chatRoom.findViewById<View>(R.id.chatBox) as EditText

        //TODO(duplicate and hardcoded for testing must delete later)
        var partner = "hUMw9apo4cPzwAExgqo1gYM56aK2"
        if (chatRoom.userId != partner)
        {
            partner = "dlXdQwKlOkQ5PWatYVQvlEOlKpy1"
        }

        chatRoom.database
            .students()
            .userId()
            .chats()
            .chatWith(partner)
            .messages()
            .build().add(Message(text = chatBox.text.toString()))
            .addOnSuccessListener { chatBox.setText("")}
            .addOnFailureListener {
                Toast.makeText(chatRoom, R.string.something_went_wrong, Toast.LENGTH_LONG).show() }

        chatRoom.database
            .students()
            .chatWith(partner)
            .chats()
            .chatWith(chatRoom.userId!!)
            .messages()
            .build().add(Message(text = chatBox.text.toString()))
            .addOnSuccessListener { chatBox.setText("")}
            .addOnFailureListener {
                Toast.makeText(chatRoom, R.string.something_went_wrong, Toast.LENGTH_LONG).show() }
    }

}