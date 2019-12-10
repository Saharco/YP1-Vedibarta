package com.technion.vedibarta.chatRoom

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
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
import com.technion.vedibarta.utilities.VedibartaActivity
import kotlinx.android.synthetic.main.activity_chat_room.*
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.SetOptions
import com.technion.vedibarta.R
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class ChatRoomActivity : VedibartaActivity()
    ,ChatRoomQuestionGeneratorDialog.QuestionGeneratorDialogListener
    ,ChatRoomAbuseReportDialog.AbuseReportDialogListener
{

    private lateinit var adapter: FirestoreRecyclerAdapter<Message, RecyclerView.ViewHolder>
    private var chatPartnerId: String? = "hNApDXaHOUi7lRB5qYNs" //TODO(this only temporary value, set this right on activity creation before adapter is configured)
    // private var chatPartnerId: String? = intent.getStringExtra("id") //TODO use this instead of the above
    private val photoUrl: String? = intent.getStringExtra("photoUrl")
    private val dateFormatter = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
    private val dayFormatter = SimpleDateFormat("dd", Locale.getDefault())
    private val currentDate = Date(System.currentTimeMillis())


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(com.technion.vedibarta.R.layout.activity_chat_room)

        setToolbar(chatToolbar)
        configureAdapter()
        buttonChatBoxSend.setOnClickListener { sendMessage(it) }
        popupMenu.setOnClickListener{ showPopup(it) }
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    private fun configureAdapter()
    {
        //TODO(for testing, must delete later)
        var partner = "hUMw9apo4cPzwAExgqo1gYM56aK2"
        if (userId == partner)
        {
            partner = "dlXdQwKlOkQ5PWatYVQvlEOlKpy1"
        }
        val query = database.students().userId().chats().chatWith(partner).messages()
                                    .build().orderBy("fullTimeStamp")
        val options = FirestoreRecyclerOptions.Builder<Message>()
            .setQuery(query,Message::class.java)
            .build()
        adapter = getChatAdapter(options)
        chatView.layoutManager = LinearLayoutManager(this)
        chatView.adapter = adapter
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

    private fun getChatAdapter(options: FirestoreRecyclerOptions<Message>): FirestoreRecyclerAdapter<Message, RecyclerView.ViewHolder>
    {
       return  object: FirestoreRecyclerAdapter<Message, RecyclerView.ViewHolder>(options)
        {
            override fun getItemViewType(position: Int): Int
            {
                return this.snapshots[position].messageType.ordinal
            }

            override fun onDataChanged()
            {
                super.onDataChanged()
                val lastVisiblePosition = (chatView.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition()
                if (this.itemCount - lastVisiblePosition <= 2)
                    chatView.smoothScrollToPosition(this.itemCount)
            }
            
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): RecyclerView.ViewHolder {
                var view: View? = null
                when (viewType) {
                    MessageType.USER.ordinal -> {
                        view = LayoutInflater.from(parent.context).inflate(
                            R.layout.sent_message_holder,
                            parent,
                            false
                        )
                        return SentMessageViewHolder(view)
                    }
                    MessageType.OTHER.ordinal -> {
                        view = LayoutInflater.from(parent.context).inflate(
                            R.layout.received_message_holder,
                            parent,
                            false
                        )
                        return ReceivedMessageViewHolder(view)
                    }
                    else -> {
                        view = LayoutInflater.from(parent.context).inflate(
                            R.layout.generator_message_holder,
                            parent,
                            false
                        )
                        return GeneratorMessageViewHolder(view)
                    }
                }
            }

            override fun onBindViewHolder(
                holder: RecyclerView.ViewHolder,
                position: Int,
                message: Message
            ) {
                when (holder) {
                    is SentMessageViewHolder -> holder.bind(message)
                    is ReceivedMessageViewHolder -> holder.bind(message)
                    is GeneratorMessageViewHolder -> holder.bind(message)
                }
            }
        }
    }

    private class SentMessageViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {

        fun bind(message: Message) {
            itemView.findViewById<TextView>(R.id.sentMessageBody).text = message.text
            itemView.findViewById<TextView>(R.id.sentMessageTime).text = message.getTime()
        }
    }

    private class ReceivedMessageViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {

        fun bind(message: Message) {
            itemView.findViewById<TextView>(R.id.receivedMessageBody).text = message.text
            itemView.findViewById<TextView>(R.id.receivedMessageTime).text = message.getTime()
        }
    }
    private class GeneratorMessageViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {

        fun bind(message: Message) {
            itemView.findViewById<TextView>(R.id.generatorMessageBody).text = message.text
        }
    }

    private fun sendMessage(v: View)
    {
        chatView.smoothScrollToPosition(adapter.itemCount)
        val text: String = chatBox.text.toString()
        if (text.isEmpty())
            return


        //TODO(duplicate and hardcoded for testing must delete later)
        var partner = "hUMw9apo4cPzwAExgqo1gYM56aK2"
        if (userId == partner)
        {
            partner = "dlXdQwKlOkQ5PWatYVQvlEOlKpy1"
        }

        val lastMessageDate: Date? = adapter.snapshots.lastOrNull()?.fullTimeStamp
        if (lastMessageDate != null)
        {
            val timeGap = currentDate.time - lastMessageDate.time
            val dayGap = (dayFormatter.format(currentDate).toInt() - dayFormatter.format(lastMessageDate).toInt())
            if (TimeUnit.DAYS.convert(timeGap, TimeUnit.MILLISECONDS) >= 1 || dayGap >= 1)
            {
                duplicateWrite(userId!!, partner, dateFormatter.format(currentDate),true)
            }
        }
        duplicateWrite(userId!!, partner, text, false)
        chatBox.setText("")
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
