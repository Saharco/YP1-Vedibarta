package com.technion.vedibarta.teacher

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FieldValue
import com.google.firebase.iid.FirebaseInstanceId
import com.miguelcatalan.materialsearchview.MaterialSearchView
import com.technion.vedibarta.POJOs.Chat
import com.technion.vedibarta.POJOs.ChatMetadata
import com.technion.vedibarta.POJOs.Gender
import com.technion.vedibarta.R
import com.technion.vedibarta.database.DatabaseVersioning
import com.technion.vedibarta.databinding.FragmentTeacherHomeBinding
import com.technion.vedibarta.fragments.ChatListFragment
import com.technion.vedibarta.utilities.VedibartaActivity
import kotlinx.android.synthetic.main.fragment_chat_list.*
import java.util.HashMap
import com.google.android.gms.tasks.OnCompleteListener
import com.technion.vedibarta.main.*
import com.technion.vedibarta.utilities.VedibartaActivity.Companion.userId
import kotlinx.android.synthetic.main.fragment_teacher_home.*

private const val TITLE = "ודיברת"

/**
 * A simple [Fragment] subclass.
 * Use the [TeacherHomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TeacherHomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var title: String? = null
    private val chatPartnersMap = HashMap<String, ChatMetadata>()
    private var mainAdapter: MainAdapter? = null
    private lateinit var searchAdapter: MainsSearchAdapter<String>

    companion object
    {
        const val TAG = "Vedibarta/chat-lobby"

        @JvmStatic
        fun newInstance(title: String) =
            TeacherHomeFragment().apply {
                arguments = Bundle().apply {
                    putString(TITLE, title)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            title = it.getString(TITLE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentTeacherHomeBinding.inflate(inflater, container, false)
        binding.toolbarTitle.text = title
        binding.extendedFloatingActionButton.setOnClickListener(
            Navigation.createNavigateOnClickListener(R.id.action_global_teacherSearchMatchFragment)
        )

        val chatList = binding.teacherChatHistory
        chatList.layoutManager = LinearLayoutManager(requireContext())
        mainAdapter = getMainAdapter(chatList)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateUserToken()
    }

    override fun onStart() {
        super.onStart()
        if (mainAdapter?.itemCount == 0)
            teacherChatHistory.visibility = View.GONE

        mainAdapter!!.startListening()
    }

    override fun onStop() {
        super.onStop()
        mainAdapter!!.stopListening()
    }

    private fun updateUserToken() {
        FirebaseInstanceId.getInstance()
            .instanceId.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful or (task.result == null)) {
                    Log.d(ChatListFragment.TAG, "getInstanceId failed")
                    return@OnCompleteListener
                }
                val token = task.result!!.token
                Log.d(ChatListFragment.TAG, "Token is: $token")
                DatabaseVersioning.currentVersion.instance.collection("teachers").document(userId!!)
                    .update("tokens", FieldValue.arrayUnion(token))
            })
    }

    private fun onChatPopulate(): RecyclerView.AdapterDataObserver {
        return object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                if (teacherChatHistory != null)
                    teacherChatHistory.visibility = View.VISIBLE
            }
        }
    }

    private fun getMainAdapter(recycler: RecyclerView): MainAdapter {
        val adapterQuery = VedibartaActivity.database.chats().build().whereArrayContains("participantsId", userId!!)

        val options =
            FirestoreRecyclerOptions.Builder<Chat>().setQuery(adapterQuery, Chat::class.java)
                .build()

        val adapter = MainFireBaseAdapter(
            userId,
            requireActivity().applicationContext,
            chatPartnersMap,
            activity as TeacherMainActivity,
            recycler,
            options
        )

        adapter.registerAdapterDataObserver(onChatPopulate())
        return adapter
    }
}
