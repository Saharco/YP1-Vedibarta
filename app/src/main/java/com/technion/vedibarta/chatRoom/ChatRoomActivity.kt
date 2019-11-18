package com.technion.vedibarta.chatRoom

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.technion.vedibarta.R
import com.technion.vedibarta.utilities.VedibartaActivity
import kotlinx.android.synthetic.main.activity_chat_room.*

class ChatRoomActivity : VedibartaActivity()
    ,ChatRoomQuestionGeneratorDialog.QuestionGeneratorDialogListener
    ,ChatRoomAbuseReportDialog.AbuseReportDialogListener
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)

        val chatRoomListeners = ChatRoomListeners(this@ChatRoomActivity, supportFragmentManager)

        setToolbar(chatToolbar)
        chatRoomListeners.configureListeners()
        val messageList = ArrayList<Message>()
        populateMessageList(messageList)// TODO remove after testing
        configureAdapter(messageList)
    }

    //TODO remove after testing
    private fun populateMessageList(messageList: ArrayList<Message>)
    {
        val m1 = Message("hello")
        val m2 = Message("Bye")
        m2.userId = 1

        messageList.add(m1)
        messageList.add(m2)
    }

    private fun configureAdapter(messageList: ArrayList<Message>)
    {
        chatView.layoutManager = LinearLayoutManager(this)
        chatView.adapter = ChatRoomMessageAdapter(messageList){Unit}
    }

    private fun setToolbar(tb: Toolbar) {
        setSupportActionBar(tb)
        supportActionBar?.setDisplayShowTitleEnabled(false) // if you want to to write your own title programmatically
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            else -> TODO()
        }
        return true
    }

    override fun onQuestionclick(dialog: DialogFragment, v: View)
    {
        try
        {
            val question = (v as TextView).text
            Toast.makeText(this, question, Toast.LENGTH_SHORT).show()
        }
        catch (e: ClassCastException)
        {
            Log.d("QuestionGenerator", e.toString())
        }
    }

    override fun onAbuseTypeClick(dialog: DialogFragment)
    {
        Toast.makeText(this, "abuse", Toast.LENGTH_SHORT).show()
    }
}
