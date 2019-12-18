package com.technion.vedibarta.main

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.miguelcatalan.materialsearchview.MaterialSearchView
import com.technion.vedibarta.ExtentionFunctions.getName
import com.technion.vedibarta.ExtentionFunctions.getPartnerId
import com.technion.vedibarta.POJOs.ChatCard
import com.technion.vedibarta.POJOs.ChatMetadata
import com.technion.vedibarta.POJOs.Gender
import com.technion.vedibarta.R
import com.technion.vedibarta.chatRoom.ChatRoomActivity
import com.technion.vedibarta.chatSearch.ChatSearchActivity
import com.technion.vedibarta.userProfile.UserProfileActivity
import com.technion.vedibarta.utilities.VedibartaActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class MainActivity : VedibartaActivity() {

    private lateinit var adapter: FirestoreRecyclerAdapter<ChatCard, RecyclerView.ViewHolder>
    private lateinit var searchAdapter: RecyclerView.Adapter<ViewHolder>

    private val chatPartnersMap = HashMap<String, ChatMetadata>()

    companion object {
        const val TAG = "Vedibarta/chat-lobby"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.main_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        // Update user's tokens
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful || task.result == null) {
                    Log.d(TAG, "getInstanceId failed")
                    return@OnCompleteListener
                }
                val token = task.result!!.token
                Log.d(TAG, "Token is: $token")
                FirebaseFirestore.getInstance()
                    .collection("students")
                    .document(userId!!)
                    .update("tokens", FieldValue.arrayUnion(token))
            })

        extendedFloatingActionButton.setOnClickListener {
            startActivity(Intent(this, ChatSearchActivity::class.java))
        }

        configureSearchView()
    }

    private fun configureSearchView() {
        searchView.setOnSearchViewListener(object : MaterialSearchView.SearchViewListener {
            override fun onSearchViewClosed() {
                // do nothing
            }

            override fun onSearchViewShown() {
                searchView.setOnQueryTextListener(object : MaterialSearchView.OnQueryTextListener {

                    override fun onQueryTextSubmit(query: String?): Boolean {
                        Log.i(TAG, "textSubmit: $query")
                        hideKeyboard(this@MainActivity)
                        return true
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        if (newText == null || newText == "") {
                            showAllChats()
                        }
                        else
                            showFilteredChats(newText)
                        return false
                    }
                })
            }
        })
    }

    private fun showFilteredChats(query: String) {
        Log.d(TAG, "changing to filtered query")

        adapter.stopListening()

        //TODO: change this mapping to descending order based on last activity of the chat
        var i = 0
        val filteredMap = chatPartnersMap.filterKeys { it.startsWith(query, ignoreCase = true) }
            .mapKeys { i++ }

        searchAdapter = object : RecyclerView.Adapter<ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                val userNameView =
                    LayoutInflater.from(parent.context).inflate(R.layout.chat_card, parent, false)
                return ViewHolder(userNameView, userId!!, applicationContext)
            }

            override fun getItemCount(): Int {
                return filteredMap.size
            }

            override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                val chatMetadata = filteredMap.getValue(holder.adapterPosition)
                holder.bind(chatMetadata)
                holder.view.setOnClickListener {
                    val intent = Intent(this@MainActivity, ChatRoomActivity::class.java)
                    intent.putExtra("chatData", chatMetadata)
                    startActivity(intent)
                }
            }
        }
        chat_history.layoutManager = LinearLayoutManager(this)
        chat_history.adapter = searchAdapter
    }

    private fun showAllChats() {
        Log.d(TAG, "showing all chat results")

        val adapterQuery = database.chats().build().whereArrayContains("participantsId", userId!!)
        val options = FirestoreRecyclerOptions.Builder<ChatCard>()
            .setQuery(adapterQuery, ChatCard::class.java)
            .build()
        adapter = getAdapter(options)
        chat_history.layoutManager = LinearLayoutManager(this)
        chat_history.adapter = adapter
        adapter.startListening()
    }


    override fun onStart() {
        super.onStart()
        adapter.startListening()
        showAllChats()
    }

    override fun onStop() {
        super.onStop()
        searchView.closeSearch()
        adapter.stopListening()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        searchView.setMenuItem(menu.findItem(R.id.search))
        return true
    }


    override fun onBackPressed() {
        if (searchView.isSearchOpen)
            searchView.closeSearch()
        else
            super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_user_profile ->
                startActivity(Intent(this, UserProfileActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    private class ViewHolder(val view: View, val userId: String, val context: Context) :
        RecyclerView.ViewHolder(view) {
        private fun getString(x: Int): String {
            return context.resources.getString(x)
        }

        private fun calcRelativeTime(time: Date): String {
            try {
                val current = Date(System.currentTimeMillis())
                val timeGap = current.time - time.time
                return when (val hoursGap =
                    TimeUnit.HOURS.convert(timeGap, TimeUnit.MILLISECONDS)) {
                    in 0..1 -> getString(R.string.just_now)
                    in 2..24 -> "${getString(R.string.sent)} ${getString(R.string.before)} $hoursGap ${getString(
                        R.string.hours
                    )}"
                    in 2..168 -> "${getString(R.string.sent)} ${getString(R.string.before)} ${hoursGap / 24} ${getString(
                        R.string.days
                    )}"
                    else -> SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(time)
                }
            } catch (e: Exception) {
                com.technion.vedibarta.utilities.error(e, "calc")
            }
            return ""
        }

        fun bind(chatMetadata: ChatMetadata) {
            itemView.findViewById<TextView>(R.id.user_name).text = chatMetadata.partnerName
            itemView.findViewById<TextView>(R.id.last_message).text = chatMetadata.lastMessage
            itemView.findViewById<TextView>(R.id.relative_timestamp).text =
                calcRelativeTime(chatMetadata.lastMessageTimestamp)
            val profilePicture = itemView.findViewById<ImageView>(R.id.user_picture)

            if (chatMetadata.partnerPhotoUrl == null)
                displayDefaultProfilePicture(profilePicture, chatMetadata.partnerGender)
            else {

                Glide.with(context)
                    .asBitmap()
                    .load(chatMetadata.partnerPhotoUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .listener(object : RequestListener<Bitmap> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: com.bumptech.glide.request.target.Target<Bitmap>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            displayDefaultProfilePicture(profilePicture, chatMetadata.partnerGender)
                            return false
                        }

                        override fun onResourceReady(
                            resource: Bitmap?,
                            model: Any?,
                            target: com.bumptech.glide.request.target.Target<Bitmap>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            profilePicture.setImageBitmap(resource)
                            return false
                        }

                    })
                    .into(profilePicture)
            }
        }

        fun bind(card: ChatCard, photoUrl: String? = null, otherGender: Gender = Gender.MALE) {
            try {
                val partnerId = card.getPartnerId(userId)
                itemView.findViewById<TextView>(R.id.user_name).text = card.getName(partnerId)
                itemView.findViewById<TextView>(R.id.last_message).text = card.lastMessage
                itemView.findViewById<TextView>(R.id.relative_timestamp).text =
                    calcRelativeTime(card.lastMessageTimestamp)
                val profilePicture = itemView.findViewById<ImageView>(R.id.user_picture)

                if (photoUrl == null)
                    displayDefaultProfilePicture(profilePicture, otherGender)
                else {

                    Glide.with(context)
                        .asBitmap()
                        .load(photoUrl)
                        .apply(RequestOptions.circleCropTransform())
                        .listener(object : RequestListener<Bitmap> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: com.bumptech.glide.request.target.Target<Bitmap>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                displayDefaultProfilePicture(profilePicture, otherGender)
                                return false
                            }

                            override fun onResourceReady(
                                resource: Bitmap?,
                                model: Any?,
                                target: com.bumptech.glide.request.target.Target<Bitmap>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                profilePicture.setImageBitmap(resource)
                                return false
                            }

                        })
                        .into(profilePicture)
                }

            } catch (e: Exception) {
                com.technion.vedibarta.utilities.error(e)
            }
        }

        private fun displayDefaultProfilePicture(v: ImageView, otherGender: Gender) {
            when (otherGender) {
                Gender.MALE -> v.setImageResource(R.drawable.ic_photo_default_profile_man)
                Gender.FEMALE -> v.setImageResource(R.drawable.ic_photo_default_profile_girl)
                else -> Log.d(TAG, "other student is neither male nor female??")
            }
        }
    }

    private fun getAdapter(options: FirestoreRecyclerOptions<ChatCard>): FirestoreRecyclerAdapter<ChatCard, RecyclerView.ViewHolder> {
        return object : FirestoreRecyclerAdapter<ChatCard, RecyclerView.ViewHolder>(options) {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): ViewHolder {
                val userNameView =
                    LayoutInflater.from(parent.context).inflate(R.layout.chat_card, parent, false)
                return ViewHolder(userNameView, userId!!, applicationContext)
            }

            override fun onBindViewHolder(
                holder: RecyclerView.ViewHolder,
                position: Int,
                card: ChatCard
            ) {
                when (holder) {
                    is ViewHolder -> {
                        Log.d(TAG, "student is: $student")
                        FirebaseFirestore.getInstance().collection("students")
                            .document(card.getPartnerId(student!!.name))
                            .get()
                            .addOnSuccessListener { otherStudent ->
                                val otherStudentPhotoUrl = otherStudent["photo"] as String?
                                val otherGender = if (otherStudent["gender"] as String == "MALE")
                                    Gender.MALE
                                else
                                    Gender.FEMALE

                                holder.bind(card, otherStudentPhotoUrl, otherGender)
                                val partnerId = card.getPartnerId(userId!!)
                                val chatMetadata = ChatMetadata(
                                    card.chat!!,
                                    partnerId,
                                    card.getName(partnerId),
                                    card.numMessages,
                                    card.lastMessage,
                                    card.lastMessageTimestamp,
                                    otherGender,
                                    otherStudentPhotoUrl
                                )

                                chatPartnersMap[chatMetadata.partnerName] = chatMetadata

                                holder.view.setOnClickListener {
                                    val i = Intent(this@MainActivity, ChatRoomActivity::class.java)
                                    i.putExtra("chatData", chatMetadata)
                                    startActivity(i)
                                }
                            }.addOnFailureListener {
                                holder.bind(card)
                                val partnerId = card.getPartnerId(userId!!)
                                val chatMetadata = ChatMetadata(
                                    card.chat!!,
                                    partnerId,
                                    card.getName(partnerId),
                                    card.numMessages,
                                    card.lastMessage,
                                    card.lastMessageTimestamp
                                )

                                chatPartnersMap[chatMetadata.partnerName] = chatMetadata

                                holder.view.setOnClickListener {
                                    val i = Intent(this@MainActivity, ChatRoomActivity::class.java)
                                    i.putExtra("chatData", chatMetadata)
                                    startActivity(i)
                                }
                            }
                    }

                }
            }
        }
    }
}
