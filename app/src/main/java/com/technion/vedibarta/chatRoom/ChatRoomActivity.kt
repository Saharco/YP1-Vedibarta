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
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import android.media.MediaPlayer
import android.os.Handler
import java.lang.Exception
import com.technion.vedibarta.utilities.error


class ChatRoomActivity : VedibartaActivity()
    ,ChatRoomQuestionGeneratorDialog.QuestionGeneratorDialogListener
    ,ChatRoomAbuseReportDialog.AbuseReportDialogListener
{

    private lateinit var adapter: FirestoreRecyclerAdapter<Message, RecyclerView.ViewHolder>
    private var chatPartnerId: String? = "hNApDXaHOUi7lRB5qYNs" //TODO(this only temporary value, set this right on activity creation before adapter is configured)
    // private var chatPartnerId: String? = intent.getStringExtra("id") //TODO use this instead of the above
    private var photoUrl: String? = null
    private val dateFormatter = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
    private val dayFormatter = SimpleDateFormat("dd", Locale.getDefault())
    private val currentDate = Date(System.currentTimeMillis())
    private val MESSAGE_SOUND_INTERVAL: Long = 2000
    private val soundHandler = Handler()
    private val soundTask = Runnable { soundHandler.removeMessages(0) }
    private var numMessages = 0


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(com.technion.vedibarta.R.layout.activity_chat_room)
        photoUrl = intent.getStringExtra("photoUrl")
        chatPartnerId = intent.getStringExtra("id")
        numMessages =intent.getIntExtra("numMessages", 0)
        val partnerName = intent.getStringExtra("name")

        setToolbar(chatToolbar)
        configureAdapter()
        buttonChatBoxSend.setOnClickListener { sendMessage(it) }
        popupMenu.setOnClickListener{ showPopup(it) }
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

    override fun onPause()
    {
        super.onPause()
        Log.d("wtf", "onPause")
        database.students()
            .userId()
            .chats()
            .chatWith(chatPartnerId!!)
            .build().update("numMessages", numMessages)
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
        chatView.adapter = adapter
        chatView.layoutManager = LinearLayoutManager(this)
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
            private var needInitialScroll = true
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
                else
                    if (needInitialScroll)
                    {
                        needInitialScroll = false
                        chatView.smoothScrollToPosition(this.itemCount)
                    }

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

                if (!soundHandler.hasMessages(0))
                {
                    soundHandler.removeCallbacksAndMessages(null)
                    soundHandler.postDelayed(soundTask, MESSAGE_SOUND_INTERVAL)
                    try {
                        val res = tryToPlaySound(message.messageType, this.itemCount)
                    }
                    catch (e: Exception)
                    {
                        error(e, "tryToPlaySound")
                    }
                }
            }
        }
    }

    private fun tryToPlaySound(type: MessageType, count: Int): Boolean
    {
        if (count > numMessages) {
            // Update the amount of already-acknowledged messages
            numMessages = count
            // Let the handler know about the successful sound invocation, so it can lock it for a set time
            soundHandler.sendEmptyMessage(0)

            if (type == MessageType.USER)
            {
                val mp = MediaPlayer.create(applicationContext, com.technion.vedibarta.R.raw.message_sent_audio);
                mp.start();
                return true;
            } else {
                val mp = MediaPlayer.create(applicationContext, com.technion.vedibarta.R.raw.message_received_audio);
                mp.start();
                return true
            }
        }
        return false
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
        popup.inflate(com.technion.vedibarta.R.menu.chat_room_popup_menu)

        popup.setOnMenuItemClickListener { item: MenuItem? ->

            when (item!!.itemId) {
                com.technion.vedibarta.R.id.generateQuestion -> {
                    ChatRoomQuestionGeneratorDialog().show(
                        supportFragmentManager,
                        "QuestionGeneratorFragment"
                    )
                }
                com.technion.vedibarta.R.id.reportAbuse -> {
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
                Toast.makeText(this, com.technion.vedibarta.R.string.something_went_wrong, Toast.LENGTH_LONG).show()
            }
        partnerPath.set(Message(partnerMessageType, text, timeSent), SetOptions.merge())
            .addOnFailureListener {
                Toast.makeText(this, com.technion.vedibarta.R.string.something_went_wrong, Toast.LENGTH_LONG).show()
            }
    }
}
