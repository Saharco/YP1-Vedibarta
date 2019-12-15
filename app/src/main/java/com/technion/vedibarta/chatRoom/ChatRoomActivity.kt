package com.technion.vedibarta.chatRoom

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.technion.vedibarta.POJOs.Message
import com.technion.vedibarta.utilities.VedibartaActivity
import kotlinx.android.synthetic.main.activity_chat_room.*
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.technion.vedibarta.POJOs.Gender
import com.technion.vedibarta.R
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class ChatRoomActivity : VedibartaActivity()
    , ChatRoomQuestionGeneratorDialog.QuestionGeneratorDialogListener
    , ChatRoomAbuseReportDialog.AbuseReportDialogListener {
    val systemSender = "-1"
    private lateinit var adapter: FirestoreRecyclerAdapter<Message, RecyclerView.ViewHolder>
    private var chatId: String? =
        null //TODO(this only temporary value, set this right on activity creation before adapter is configured)
    private var photoUrl: String? = null
    private val dateFormatter = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
    private val dayFormatter = SimpleDateFormat("dd", Locale.getDefault())
    private val currentDate = Date(System.currentTimeMillis())
    private var numMessages = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)
        val partnerName = intent.getStringExtra("name")
        chatId = intent.getStringExtra("chatId")
        photoUrl = intent.getStringExtra("photoUrl")
        numMessages = intent.getIntExtra("numMessages", 0)

        setToolbar(chatToolbar)
        configureAdapter()
        buttonChatBoxSend.setOnClickListener { sendMessage(it) }
        popupMenu.setOnClickListener { showPopup(it) }
        if (student!!.gender == Gender.FEMALE)
            chatBox.text =
                SpannableStringBuilder(resources.getString(R.string.chat_room_enter_message_f))
        toolbarUserName.text = partnerName
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    private fun getChatId(userId: String, partnerId: String): String {
        if (userId < partnerId)
            return "$userId$partnerId"
        return "$partnerId$userId"
    }

    private fun configureAdapter() {
        val query =
            database
                .chats()
                .chatId(chatId!!)
                .messages()
                .build().orderBy("fullTimeStamp", Query.Direction.DESCENDING)

        val options =
            FirestoreRecyclerOptions.Builder<Message>()
                .setQuery(query, Message::class.java)
                .build()

        adapter = ChatRoomAdapter(this, options, numMessages)
        adapter.notifyDataSetChanged() //
        chatView.adapter = adapter
        adapter.notifyDataSetChanged() //
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        chatView.layoutManager = layoutManager
        chatView.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            chatView.scrollToPosition(0)
        }
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

    override fun onQuestionclick(dialog: DialogFragment, v: View) {
        try {
            val question = (v as TextView).text
            Toast.makeText(this, question, Toast.LENGTH_SHORT).show()
        } catch (e: ClassCastException) {
            Log.d("QuestionGenerator", e.toString())
        }
    }

    override fun onAbuseTypeClick(dialog: DialogFragment) {
        TODO("need to decide what to do")
        //Toast.makeText(this, "abuse", Toast.LENGTH_SHORT).show()
    }

    private fun showPopup(view: View) {
        val popup = PopupMenu(this, view)
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

    private fun sendMessage(v: View) {
        var text = chatBox.text.toString()
        if (text.replace(" ", "")
                .replace("\n", "")
                .replace("\t", "")
                .isEmpty() )
            return

        text = text.replace("[\n]+".toRegex(), "\n").trim()

        //TODO fix the writing to database after cloud functions implemented
        val lastMessageDate: Date? = adapter.snapshots.lastOrNull()?.fullTimeStamp
        if (lastMessageDate != null) {
            val timeGap = currentDate.time - lastMessageDate.time
            val dayGap =
                (dayFormatter.format(currentDate).toInt() - dayFormatter.format(lastMessageDate).toInt())
            if (TimeUnit.DAYS.convert(timeGap, TimeUnit.MILLISECONDS) >= 1 || dayGap >= 1) {
                write(dateFormatter.format(currentDate), true)
            }
        }
        write(text, false)
        chatBox.setText("")
    }

    private fun write(text: String, isGeneratorMessage: Boolean) {
        val timeSent = Date(System.currentTimeMillis())
        var path = database.chats()
            .chatId(chatId!!)
            .messages()
            .message(timeSent)
            .build()
        var sender = userId!!
        if (isGeneratorMessage) {
            sender = systemSender
            path = database.chats()
                .chatId(chatId!!)
                .messages()
                .systemMessage(timeSent)
                .build()
        }
        path.set(Message(sender, text, timeSent), SetOptions.merge())
            .addOnFailureListener {
                Toast.makeText(this, R.string.something_went_wrong, Toast.LENGTH_LONG).show()
            }
    }
}
