package com.technion.vedibarta.chatRoom

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
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
import com.technion.vedibarta.utilities.DocumentsCollections
import java.lang.Exception


class ChatRoomActivity : VedibartaActivity()
    ,ChatRoomQuestionGeneratorDialog.QuestionGeneratorDialogListener
    ,ChatRoomAbuseReportDialog.AbuseReportDialogListener
{

    private lateinit var adapter: FirestoreRecyclerAdapter<Message, RecyclerView.ViewHolder>
    var chatPartnerId: String? = "hNApDXaHOUi7lRB5qYNs" //TODO(this only temporary value, set this right on activity creation before adapter is configured)

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(com.technion.vedibarta.R.layout.activity_chat_room)

        val chatRoomListeners = ChatRoomListeners(this, chatPartnerId!!, supportFragmentManager)

        setToolbar(chatToolbar)
        chatRoomListeners.configureListeners()
        configureAdapter()
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
        val query = database.students().userId().chats().chatWith(chatPartnerId!!).messages()
                                    .build().limit(50).orderBy("fullTimeStamp")
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
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): RecyclerView.ViewHolder {
                var view: View? = null
                when (viewType) {
                    MessageType.USER.ordinal -> {
                        view = LayoutInflater.from(parent.context).inflate(
                            com.technion.vedibarta.R.layout.sent_message_holder,
                            parent,
                            false
                        )
                        return SentMessageViewHolder(view)
                    }
                    MessageType.OTHER.ordinal -> {
                        view = LayoutInflater.from(parent.context).inflate(
                            com.technion.vedibarta.R.layout.received_message_holder,
                            parent,
                            false
                        )
                        return ReceivedMessageViewHolder(view)
                    }
                    else -> {
                        view = LayoutInflater.from(parent.context).inflate(
                            com.technion.vedibarta.R.layout.generator_message_holder,
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
            itemView.findViewById<TextView>(com.technion.vedibarta.R.id.sentMessageBody).text = message.text
            itemView.findViewById<TextView>(com.technion.vedibarta.R.id.sentMessageTime).text = message.getTime()
        }
    }

    private class ReceivedMessageViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {

        fun bind(message: Message) {
            itemView.findViewById<TextView>(com.technion.vedibarta.R.id.receivedMessageBody).text = message.text
            itemView.findViewById<TextView>(com.technion.vedibarta.R.id.receivedMessageTime).text = message.getTime()
        }
    }
    private class GeneratorMessageViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {

        fun bind(message: Message) {
            itemView.findViewById<TextView>(com.technion.vedibarta.R.id.generatorMessageBody).text = message.text
        }
    }
}
