package com.technion.vedibarta.chatRoom

import android.os.Bundle
import android.text.SpannableString
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
import com.technion.vedibarta.POJOs.MessageType
import com.technion.vedibarta.R
import com.technion.vedibarta.utilities.Gender
import com.technion.vedibarta.utilities.VedibartaActivity
import kotlinx.android.synthetic.main.activity_chat_room.*
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class ChatRoomActivity : VedibartaActivity()
    ,ChatRoomQuestionGeneratorDialog.QuestionGeneratorDialogListener
    ,ChatRoomAbuseReportDialog.AbuseReportDialogListener
{

    private lateinit var adapter: FirestoreRecyclerAdapter<Message, RecyclerView.ViewHolder>
    private var chatPartnerId: String? = "hNApDXaHOUi7lRB5qYNs" //TODO(this only temporary value, set this right on activity creation before adapter is configured)
    private var photoUrl: String? = null
    private val dateFormatter = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
    private val dayFormatter = SimpleDateFormat("dd", Locale.getDefault())
    private val currentDate = Date(System.currentTimeMillis())
    private var numMessages = 0


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)
        photoUrl = intent.getStringExtra("photoUrl")
        chatPartnerId = intent.getStringExtra("id")
        numMessages =intent.getIntExtra("numMessages", 0)
        val partnerName = intent.getStringExtra("name")

        setToolbar(chatToolbar)
        configureAdapter()
        buttonChatBoxSend.setOnClickListener { sendMessage(it) }
        popupMenu.setOnClickListener{ showPopup(it) }
        if (student!!.gender == Gender.FEMALE)
            chatBox.text = SpannableStringBuilder(resources.getString(R.string.chat_room_enter_message_f))
        toolbarUserName.text = partnerName
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
        chatRoomListeners.configureListeners()
        val messageList = ArrayList<Message>()
        populateMessageList(messageList)// TODO remove after testing

        configureAdapter(messageList)
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    private fun configureAdapter()
    {
        val query =
            database
                .students()
                .userId()
                .chats()
                .chatWith(chatPartnerId!!)
                .messages()
                .build().orderBy("fullTimeStamp")

        val options =
            FirestoreRecyclerOptions.Builder<Message>()
            .setQuery(query,Message::class.java)
            .build()

        val initialChatViewPopulationListener = object: View.OnLayoutChangeListener{
            override fun onLayoutChange(p0: View?, p1: Int, p2: Int, p3: Int, p4: Int,p5: Int, p6: Int, p7: Int, p8: Int)
            {
                if (adapter.itemCount > 0)
                {
                    chatView.smoothScrollToPosition(adapter.itemCount)
                    chatView.removeOnLayoutChangeListener(this)
                }
            }
        }

        adapter = ChatRoomAdapter(this, options, numMessages)
        chatView.adapter = adapter
        chatView.layoutManager = LinearLayoutManager(this)
        chatView.addOnLayoutChangeListener(initialChatViewPopulationListener)
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
        TODO("need to decide what to do")
        //Toast.makeText(this, "abuse", Toast.LENGTH_SHORT).show()
    }

    private fun showPopup(view: View)
    {
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

    private fun sendMessage(v: View)
    {
        chatView.smoothScrollToPosition(adapter.itemCount)
        val text: String = chatBox.text.toString()
        if (text.isEmpty())
            return

        //TODO fix the writing to database after cloud functions implemented
        val lastMessageDate: Date? = adapter.snapshots.lastOrNull()?.fullTimeStamp
        if (lastMessageDate != null)
        {
            val timeGap = currentDate.time - lastMessageDate.time
            val dayGap = (dayFormatter.format(currentDate).toInt() - dayFormatter.format(lastMessageDate).toInt())
            if (TimeUnit.DAYS.convert(timeGap, TimeUnit.MILLISECONDS) >= 1 || dayGap >= 1)
            {
                duplicateWrite(userId!!, chatPartnerId!!, dateFormatter.format(currentDate),true)
            }
        }
        duplicateWrite(userId!!, chatPartnerId!!, text, false)
        chatBox.setText("")
    }

    private fun duplicateWrite(userId: String, partnerId: String, text: String, isGeneratorMessage: Boolean)
    {
        val timeSent = Date(System.currentTimeMillis())
        var userPath  = database.students()
                                        .userId()
                                        .chats()
                                        .chatWith(partnerId)
                                        .messages()
                                        .message(timeSent)
                                        .build()
        var partnerPath = database.students()
                                                .chatWith(partnerId)
                                                .chats()
                                                .chatWith(userId)
                                                .messages()
                                                .message(timeSent)
                                                .build()
        var userMassageType = MessageType.USER
        var partnerMessageType = MessageType.OTHER
        if (isGeneratorMessage)
        {
            userMassageType = MessageType.GENERATOR
            partnerMessageType = MessageType.GENERATOR

            userPath = database.students()
                .userId()
                .chats()
                .chatWith(partnerId)
                .messages()
                .systemMessage(timeSent)
                .build()

            partnerPath = database.students()
                .chatWith(partnerId)
                .chats()
                .chatWith(userId)
                .messages()
                .systemMessage(timeSent)
                .build()
        }
        userPath.set(Message(userMassageType, text, timeSent), SetOptions.merge())
            .addOnFailureListener {
                Toast.makeText(this, R.string.something_went_wrong, Toast.LENGTH_LONG).show()
            }
        partnerPath.set(Message(partnerMessageType, text, timeSent), SetOptions.merge())
            .addOnFailureListener {
                Toast.makeText(this, R.string.something_went_wrong, Toast.LENGTH_LONG).show()
            }
    }
}
